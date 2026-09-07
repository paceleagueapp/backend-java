package com.paceleague.territory.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.season.application.port.in.shared.GetCurrentSeasonPort;
import com.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult.CapturedTerritory;
import com.paceleague.territory.application.dto.TerritoryHexOwnership;
import com.paceleague.territory.application.port.in.shared.ProcessTerritoryRunUseCase;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.paceleague.territory.domain.entity.TerritoryHex;
import com.paceleague.territory.domain.policy.ClosedLoopDetector;
import com.paceleague.territory.domain.policy.H3TerritoryGrid;
import com.paceleague.territory.domain.policy.PolygonGeometry;
import com.paceleague.territory.domain.policy.TerritoryClaimValidator;
import com.uber.h3core.H3Core;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProcessTerritoryRunService implements ProcessTerritoryRunUseCase {

    private final TerritoryRepositoryPort territoryRepositoryPort;
    private final TerritoryHexRepositoryPort territoryHexRepositoryPort;
    private final GetCurrentSeasonPort getCurrentSeasonPort;
    private final GetMemberNicknamePort getMemberNicknamePort;
    private final TerritoryProperties props;
    private final H3Core h3Core;
    private final ObjectMapper objectMapper;

    // REQUIRES_NEW: record→rank의 ApplyScoreUseCase(호출자 트랜잭션에 합류)와 달리, 땅따먹기 처리 실패가
    // 러닝 기록 저장을 롤백시키면 안 되므로 별도 트랜잭션으로 분리한다. 호출자(SaveGpsSessionService)는
    // 예외를 잡아 삼킨다("닫힌 도형 아님"은 애초에 예외가 아니라 NO_LOOP 결과로 반환).
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessTerritoryRunResult process(ProcessTerritoryRunCommand command) {
        List<double[]> coords = command.coords();
        if (!ClosedLoopDetector.isClosedLoop(coords, props.closeThresholdMeters())) {
            return ProcessTerritoryRunResult.noLoop();
        }

        PolygonGeometry polygon;
        try {
            polygon = PolygonGeometry.fromLatLngRing(coords);
        } catch (IllegalArgumentException e) {
            return ProcessTerritoryRunResult.invalidShape();
        }

        double areaSqm = polygon.areaSqm();
        double perimeterM = polygon.perimeterMeters();
        if (!TerritoryClaimValidator.isClaimable(perimeterM, areaSqm,
                props.minPerimeterMeters(), props.minAreaSqm(), props.maxAreaSqm())) {
            return ProcessTerritoryRunResult.invalidShape();
        }

        // 실제 뛴 도형(검증 통과)을 H3(resolution 12) 헥사곤 집합으로 변환 — 안에 완전히 포함되거나
        // 경계에 걸쳐진 헥사곤까지 전부(CONTAINMENT_OVERLAPPING). 소유권/겹침 판정은 이제부터 이 집합 기준.
        List<Long> coveredHexes = H3TerritoryGrid.coverRing(h3Core, polygon.ring(), props.hexResolution());
        if (coveredHexes.isEmpty()) {
            return ProcessTerritoryRunResult.invalidShape();
        }

        // 헥사곤 단위로 "이미 누군가 소유" 여부를 조회 — territory 단위 집계가 아니라 헥사곤 하나하나의
        // 소유자를 알아야 "겹친 부분만" 뺏을 수 있다(2026-09-07, 이전엔 territory_sno별 겹침 개수만 셌다).
        List<TerritoryHexOwnership> owned = territoryHexRepositoryPort.findActiveOwners(coveredHexes);
        if (owned.isEmpty()) {
            Long createdSno = createTerritoryFor(command, perimeterM, coveredHexes);
            return ProcessTerritoryRunResult.created(createdSno);
        }
        return captureAndClaim(command, perimeterM, coveredHexes, owned);
    }

    // HP 없음(2026-09-05 제거), 겹침 판정은 헥사곤 단위(2026-09-07 변경):
    //  - 이미 내가 가진 헥사곤 → 그대로 둔다(내 다른 territory 소속이라 중복 편입 금지).
    //  - 남이 가진 헥사곤 → 그 헥사곤만 원래 땅에서 떼어내 이번 러너 몫으로 옮긴다("먼저 점령한 사람"이
    //    아니라 "지금 이 헥사곤을 실제로 뛴 사람" 기준 — 나중에 뛴 사람이 항상 이긴다). 원래 땅은 남은
    //    헥사곤만으로 도형이 줄어들고(shrinkOrRelease), 전부 뺏기면 그 행 자체가 삭제된다.
    //  - 비어 있던 헥사곤 → 그대로 이번 러너 몫.
    // 뺏은 헥사곤 + 빈 헥사곤을 합쳐 이번 러닝의 새 territory 하나로 편입한다(이미 내 것인 헥사곤은 제외).
    private ProcessTerritoryRunResult captureAndClaim(ProcessTerritoryRunCommand command, double perimeterM,
                                                       List<Long> coveredHexes, List<TerritoryHexOwnership> owned) {
        Set<Long> ownedHexSet = new HashSet<>();
        Map<Long, List<Long>> hexesByTargetSno = new LinkedHashMap<>();
        for (TerritoryHexOwnership o : owned) {
            ownedHexSet.add(o.h3Index());
            hexesByTargetSno.computeIfAbsent(o.territorySno(), k -> new ArrayList<>()).add(o.h3Index());
        }

        List<Long> targetSnos = new ArrayList<>(hexesByTargetSno.keySet());
        List<Territory> targets = territoryRepositoryPort.findAllByIdForUpdate(targetSnos);
        Map<Long, List<Long>> targetOwnedHexIndexes = groupHexIndexesByTerritory(
                territoryHexRepositoryPort.findByTerritorySnoIn(targetSnos));

        List<Long> newlyClaimedHexes = new ArrayList<>();
        for (Long h : coveredHexes) {
            if (!ownedHexSet.contains(h)) {
                newlyClaimedHexes.add(h); // 비어 있던 헥사곤
            }
        }

        List<CapturedTerritory> captured = new ArrayList<>();
        for (Territory target : targets) {
            List<Long> hexesTaken = hexesByTargetSno.getOrDefault(target.getSno(), List.of());
            if (hexesTaken.isEmpty() || target.isOwnedBy(command.memberSno())) {
                continue; // 내 땅과 겹친 부분은 그대로 둔다
            }
            Long previousOwner = target.getOwnerMemberSno();
            newlyClaimedHexes.addAll(hexesTaken);
            shrinkOrRelease(target, hexesTaken, targetOwnedHexIndexes.getOrDefault(target.getSno(), List.of()));
            captured.add(new CapturedTerritory(target.getSno(), previousOwner, nicknameOf(previousOwner)));
        }

        Long createdSno = null;
        if (!newlyClaimedHexes.isEmpty()) {
            createdSno = createTerritoryFor(command, perimeterM, newlyClaimedHexes);
        }
        return ProcessTerritoryRunResult.interacted(createdSno, captured, List.of(), List.of());
    }

    // 대상 territory가 소유한 헥사곤 중 이번에 뺏긴 것을 뺀 나머지로 도형을 다시 계산한다.
    // 남은 헥사곤이 없으면(가진 헥사곤을 전부 뺏김) 이 행 자체를 지운다.
    private void shrinkOrRelease(Territory target, List<Long> capturedHexIndexes, List<Long> ownedHexIndexes) {
        Set<Long> capturedSet = new HashSet<>(capturedHexIndexes);
        List<Long> remaining = ownedHexIndexes.stream().filter(h -> !capturedSet.contains(h)).toList();
        if (remaining.isEmpty()) {
            territoryRepositoryPort.delete(target);
            return;
        }
        double remainingAreaSqm = H3TerritoryGrid.totalAreaSqm(h3Core, remaining);
        List<double[]> unionRing = H3TerritoryGrid.unionBoundaryLatLng(h3Core, remaining);
        double[] bbox = bboxOf(unionRing);
        target.recomputeFromHexes(remaining.size(), remainingAreaSqm, writeRing(unionRing),
                bbox[0], bbox[1], bbox[2], bbox[3]);
        territoryRepositoryPort.save(target);
    }

    // 헥사곤 인덱스 집합(빈 땅 + 남에게서 뺏은 땅) 하나를 이번 러너 소유의 새 territory 한 건으로 편입한다.
    private Long createTerritoryFor(ProcessTerritoryRunCommand command, double perimeterM, List<Long> hexIndexes) {
        double hexAreaSqm = H3TerritoryGrid.totalAreaSqm(h3Core, hexIndexes);
        List<double[]> unionRing = H3TerritoryGrid.unionBoundaryLatLng(h3Core, hexIndexes);
        double[] bbox = bboxOf(unionRing);
        double[] centroid = H3TerritoryGrid.centroidLatLng(h3Core, hexIndexes);
        Long seasonNo = seasonNumber();

        Territory created = Territory.builder()
                .ownerMemberSno(command.memberSno())
                .season(seasonNo)
                .polygonJson(writeRing(unionRing))
                .bboxMinLat(bd(bbox[0])).bboxMinLng(bd(bbox[1]))
                .bboxMaxLat(bd(bbox[2])).bboxMaxLng(bd(bbox[3]))
                .centerLat(bd(centroid[0])).centerLng(bd(centroid[1]))
                .areaSqm(BigDecimal.valueOf(hexAreaSqm)).perimeterM(BigDecimal.valueOf(perimeterM))
                .hexCount(hexIndexes.size())
                .sourceRecordSno(command.recordSno()).sourceTrackSno(command.trackSno())
                .build();
        created = territoryRepositoryPort.save(created);

        Long createdSno = created.getSno();
        List<TerritoryHex> hexRows = new ArrayList<>(hexIndexes.size());
        for (Long h3Index : hexIndexes) {
            hexRows.add(TerritoryHex.of(h3Index, createdSno, seasonNo));
        }
        // h3_index가 PK라 saveAll은 새 헥사곤은 insert, 다른 땅에서 뺏어온 헥사곤은 territory_sno만
        // update(merge)한다 — 같은 호출로 "빈 땅 편입"과 "뺏은 땅 이전"을 동시에 처리한다.
        territoryHexRepositoryPort.saveAll(hexRows);

        return createdSno;
    }

    private static Map<Long, List<Long>> groupHexIndexesByTerritory(List<TerritoryHex> hexes) {
        Map<Long, List<Long>> grouped = new LinkedHashMap<>();
        for (TerritoryHex hex : hexes) {
            grouped.computeIfAbsent(hex.getTerritorySno(), k -> new ArrayList<>()).add(hex.getH3Index());
        }
        return grouped;
    }

    private static double[] bboxOf(List<double[]> ring) {
        double minLat = Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        for (double[] p : ring) {
            minLat = Math.min(minLat, p[0]);
            maxLat = Math.max(maxLat, p[0]);
            minLng = Math.min(minLng, p[1]);
            maxLng = Math.max(maxLng, p[1]);
        }
        return new double[]{minLat, minLng, maxLat, maxLng};
    }

    private String nicknameOf(Long memberSno) {
        try {
            return getMemberNicknamePort.getNickname(memberSno);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Long seasonNumber() {
        try {
            return getCurrentSeasonPort.getCurrentSeason().getSeason();
        } catch (RuntimeException e) {
            return null; // 시즌 미설정이어도 땅 생성은 막지 않는다
        }
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }

    private String writeRing(List<double[]> ring) {
        double[][] arr = ring.toArray(new double[0][]);
        try {
            return objectMapper.writeValueAsString(arr);
        } catch (Exception e) {
            throw new IllegalStateException("failed to serialize territory polygon", e);
        }
    }
}

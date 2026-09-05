package com.paceleague.territory.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.season.application.port.in.shared.GetCurrentSeasonPort;
import com.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult.CapturedTerritory;
import com.paceleague.territory.application.dto.TerritoryHexOverlap;
import com.paceleague.territory.application.port.in.shared.ProcessTerritoryRunUseCase;
import com.paceleague.territory.application.port.out.TerritoryContributionRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.paceleague.territory.domain.entity.TerritoryContribution;
import com.paceleague.territory.domain.entity.TerritoryHex;
import com.paceleague.territory.domain.policy.ClosedLoopDetector;
import com.paceleague.territory.domain.policy.H3TerritoryGrid;
import com.paceleague.territory.domain.policy.PolygonGeometry;
import com.paceleague.territory.domain.policy.TerritoryClaimValidator;
import com.paceleague.territory.domain.policy.TerritoryDamagePolicy;
import com.uber.h3core.H3Core;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcessTerritoryRunService implements ProcessTerritoryRunUseCase {

    private final TerritoryRepositoryPort territoryRepositoryPort;
    private final TerritoryHexRepositoryPort territoryHexRepositoryPort;
    private final TerritoryContributionRepositoryPort contributionRepositoryPort;
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

        List<TerritoryHexOverlap> overlaps = territoryHexRepositoryPort.findActiveOverlapCounts(coveredHexes);
        if (overlaps.isEmpty()) {
            return createNewTerritory(command, polygon, perimeterM, coveredHexes);
        }
        return interactWithExisting(command, overlaps);
    }

    private ProcessTerritoryRunResult interactWithExisting(ProcessTerritoryRunCommand command,
                                                             List<TerritoryHexOverlap> overlaps) {
        Map<Long, Long> overlapBySno = new LinkedHashMap<>();
        for (TerritoryHexOverlap o : overlaps) {
            overlapBySno.put(o.territorySno(), o.overlapHexCount());
        }
        List<Territory> targets = territoryRepositoryPort.findAllByIdForUpdate(new ArrayList<>(overlapBySno.keySet()));

        List<Long> damaged = new ArrayList<>();
        List<CapturedTerritory> captured = new ArrayList<>();
        List<Long> healed = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Territory target : targets) {
            long overlapHexCount = overlapBySno.getOrDefault(target.getSno(), 0L);
            if (overlapHexCount <= 0) {
                continue;
            }
            long targetHexCount = target.getHexCount() == null ? overlapHexCount : target.getHexCount();

            if (target.isOwnedBy(command.memberSno())) {
                target.heal(TerritoryDamagePolicy.heal(overlapHexCount, targetHexCount, target.getMaxHp(), props.healFactor()));
                territoryRepositoryPort.save(target);
                healed.add(target.getSno());
                continue;
            }

            int dmg = TerritoryDamagePolicy.damage(overlapHexCount, targetHexCount, target.getMaxHp(), props.attackFactor());
            boolean willDeplete = (target.getHp() - dmg) <= 0;

            List<TerritoryDamagePolicy.Contribution> priorWindow = willDeplete
                    ? contributionRepositoryPort
                    .findByTerritorySnoAndCreatedAfter(target.getSno(), now.minusMinutes(props.contributionWindowMinutes()))
                    .stream()
                    .map(c -> new TerritoryDamagePolicy.Contribution(c.getMemberSno(), c.getDamage(), c.getCreateAt()))
                    .toList()
                    : List.of();

            target.applyDamage(dmg);
            contributionRepositoryPort.save(TerritoryContribution.of(target.getSno(), command.memberSno(), dmg));

            if (target.isDepleted()) {
                Long previousOwner = target.getOwnerMemberSno();
                Long newOwner = TerritoryDamagePolicy.resolveNewOwner(priorWindow, command.memberSno(), dmg, now);
                target.capture(newOwner);
                contributionRepositoryPort.deleteByTerritorySno(target.getSno());
                captured.add(new CapturedTerritory(
                        target.getSno(), previousOwner, nicknameOf(previousOwner)));
            } else {
                damaged.add(target.getSno());
            }
            territoryRepositoryPort.save(target);
        }

        return ProcessTerritoryRunResult.interacted(captured, damaged, healed);
    }

    private ProcessTerritoryRunResult createNewTerritory(ProcessTerritoryRunCommand command, PolygonGeometry polygon,
                                                          double perimeterM, List<Long> coveredHexes) {
        double hexAreaSqm = H3TerritoryGrid.totalAreaSqm(h3Core, coveredHexes);
        List<double[]> unionRing = H3TerritoryGrid.unionBoundaryLatLng(h3Core, coveredHexes);
        double[] bbox = bboxOf(unionRing); // [minLat, minLng, maxLat, maxLng]
        double[] centroid = polygon.centroidLatLng();
        Long seasonNo = seasonNumber();

        Territory created = Territory.builder()
                .ownerMemberSno(command.memberSno())
                .season(seasonNo)
                .polygonJson(writeRing(unionRing))
                .bboxMinLat(bd(bbox[0])).bboxMinLng(bd(bbox[1]))
                .bboxMaxLat(bd(bbox[2])).bboxMaxLng(bd(bbox[3]))
                .centerLat(bd(centroid[0])).centerLng(bd(centroid[1]))
                .areaSqm(BigDecimal.valueOf(hexAreaSqm)).perimeterM(BigDecimal.valueOf(perimeterM))
                .hexCount(coveredHexes.size())
                .maxHp(props.defaultMaxHp())
                .sourceRecordSno(command.recordSno()).sourceTrackSno(command.trackSno())
                .build();
        created = territoryRepositoryPort.save(created);

        Long createdSno = created.getSno();
        List<TerritoryHex> hexRows = new ArrayList<>(coveredHexes.size());
        for (Long h3Index : coveredHexes) {
            hexRows.add(TerritoryHex.of(h3Index, createdSno, seasonNo));
        }
        territoryHexRepositoryPort.saveAll(hexRows);

        return ProcessTerritoryRunResult.created(createdSno);
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

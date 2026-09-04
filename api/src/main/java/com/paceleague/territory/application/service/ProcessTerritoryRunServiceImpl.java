package com.paceleague.territory.application.service;

import com.paceleague.member.application.port.in.GetMemberNicknamePort;
import com.paceleague.season.application.port.in.GetCurrentSeasonPort;
import com.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult.CapturedTerritory;
import com.paceleague.territory.application.port.in.ProcessTerritoryRunUseCase;
import com.paceleague.territory.application.port.out.TerritoryContributionRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.paceleague.territory.domain.entity.TerritoryContribution;
import com.paceleague.territory.domain.policy.ClosedLoopDetector;
import com.paceleague.territory.domain.policy.PolygonGeometry;
import com.paceleague.territory.domain.policy.TerritoryClaimValidator;
import com.paceleague.territory.domain.policy.TerritoryDamagePolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessTerritoryRunServiceImpl implements ProcessTerritoryRunUseCase {

    private final TerritoryRepositoryPort territoryRepositoryPort;
    private final TerritoryContributionRepositoryPort contributionRepositoryPort;
    private final GetCurrentSeasonPort getCurrentSeasonPort;
    private final GetMemberNicknamePort getMemberNicknamePort;
    private final TerritoryProperties props;
    private final ObjectMapper objectMapper;

    // REQUIRES_NEW: record→rank의 ApplyScoreUseCase(호출자 트랜잭션에 합류)와 달리, 땅따먹기 처리 실패가
    // 러닝 기록 저장을 롤백시키면 안 되므로 별도 트랜잭션으로 분리한다. 호출자(SaveGpsSessionServiceImpl)는
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

        double[] bbox = polygon.bboxLatLng(); // [minLat, minLng, maxLat, maxLng]
        List<Territory> candidates = territoryRepositoryPort.findActiveIntersectingBboxForUpdate(
                bd(bbox[0]), bd(bbox[1]), bd(bbox[2]), bd(bbox[3]));

        List<Long> damaged = new ArrayList<>();
        List<CapturedTerritory> captured = new ArrayList<>();
        List<Long> healed = new ArrayList<>();
        boolean interacted = false;
        LocalDateTime now = LocalDateTime.now();

        for (Territory target : candidates) {
            double overlapSqm = polygon.intersectionAreaSqm(parseRing(target.getPolygonJson()));
            if (overlapSqm <= 0) {
                continue;
            }
            interacted = true;
            double targetAreaSqm = target.getAreaSqm() == null ? overlapSqm : target.getAreaSqm().doubleValue();

            if (target.isOwnedBy(command.memberSno())) {
                target.heal(TerritoryDamagePolicy.heal(overlapSqm, targetAreaSqm, target.getMaxHp(), props.healFactor()));
                territoryRepositoryPort.save(target);
                healed.add(target.getSno());
                continue;
            }

            int dmg = TerritoryDamagePolicy.damage(overlapSqm, targetAreaSqm, target.getMaxHp(), props.attackFactor());
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

        if (interacted) {
            return ProcessTerritoryRunResult.interacted(captured, damaged, healed);
        }

        Long seasonNo = seasonNumber();
        Territory created = Territory.builder()
                .ownerMemberSno(command.memberSno())
                .season(seasonNo)
                .polygonJson(writeRing(polygon.ring()))
                .bboxMinLat(bd(bbox[0])).bboxMinLng(bd(bbox[1]))
                .bboxMaxLat(bd(bbox[2])).bboxMaxLng(bd(bbox[3]))
                .centerLat(bd(polygon.centroidLatLng()[0])).centerLng(bd(polygon.centroidLatLng()[1]))
                .areaSqm(BigDecimal.valueOf(areaSqm)).perimeterM(BigDecimal.valueOf(perimeterM))
                .maxHp(props.defaultMaxHp())
                .sourceRecordSno(command.recordSno()).sourceTrackSno(command.trackSno())
                .build();
        created = territoryRepositoryPort.save(created);
        return ProcessTerritoryRunResult.created(created.getSno());
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

    private List<double[]> parseRing(String polygonJson) {
        if (polygonJson == null || polygonJson.isBlank()) {
            return List.of();
        }
        try {
            double[][] arr = objectMapper.readValue(polygonJson, double[][].class);
            return new ArrayList<>(List.of(arr));
        } catch (Exception e) {
            return List.of();
        }
    }
}

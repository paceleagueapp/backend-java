package com.paceleague.territory.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.season.application.port.in.shared.GetCurrentSeasonPort;
import com.paceleague.season.domain.entity.Season;
import com.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult.Outcome;
import com.paceleague.territory.application.port.out.TerritoryContributionRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessTerritoryRunServiceTest {

    private static final double LAT0 = 37.5;
    private static final double LNG0 = 127.0;
    private static final double D = 0.002; // 약 200m — 둘레/면적 하한 통과

    @Mock
    TerritoryRepositoryPort territoryRepositoryPort;
    @Mock
    TerritoryContributionRepositoryPort contributionRepositoryPort;
    @Mock
    GetCurrentSeasonPort getCurrentSeasonPort;
    @Mock
    GetMemberNicknamePort getMemberNicknamePort;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProcessTerritoryRunService service;

    @BeforeEach
    void setUp() {
        TerritoryProperties props = new TerritoryProperties(
                null, null, null, null, null, null, null, null, null, null, null);
        service = new ProcessTerritoryRunService(
                territoryRepositoryPort, contributionRepositoryPort, getCurrentSeasonPort,
                getMemberNicknamePort, props, objectMapper);
        when(getMemberNicknamePort.getNickname(any())).thenReturn("이전주인");
        when(territoryRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Season season = new Season();
        season.setSeason(1L);
        when(getCurrentSeasonPort.getCurrentSeason()).thenReturn(season);
    }

    private static List<double[]> square(double lat, double lng) {
        List<double[]> ring = new ArrayList<>();
        ring.add(new double[]{lat, lng});
        ring.add(new double[]{lat, lng + D});
        ring.add(new double[]{lat + D, lng + D});
        ring.add(new double[]{lat + D, lng});
        ring.add(new double[]{lat, lng});
        return ring;
    }

    private String json(List<double[]> ring) throws Exception {
        return objectMapper.writeValueAsString(ring.toArray(new double[0][]));
    }

    private Territory existingTerritory(Long owner, int maxHp, List<double[]> ring) throws Exception {
        return Territory.builder()
                .ownerMemberSno(owner)
                .season(1L)
                .polygonJson(json(ring))
                .bboxMinLat(BigDecimal.valueOf(LAT0)).bboxMinLng(BigDecimal.valueOf(LNG0))
                .bboxMaxLat(BigDecimal.valueOf(LAT0 + D)).bboxMaxLng(BigDecimal.valueOf(LNG0 + D))
                .centerLat(BigDecimal.valueOf(LAT0 + D / 2)).centerLng(BigDecimal.valueOf(LNG0 + D / 2))
                .areaSqm(BigDecimal.valueOf(40_000)).perimeterM(BigDecimal.valueOf(800))
                .maxHp(maxHp)
                .build();
    }

    private ProcessTerritoryRunCommand runOver(double lat, double lng) {
        return new ProcessTerritoryRunCommand(
                100L, 500L, 900L, square(lat, lng),
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now());
    }

    @Test
    void 닫힌_도형이_아니면_아무것도_하지_않는다() {
        List<double[]> openPath = new ArrayList<>(square(LAT0, LNG0));
        openPath.remove(openPath.size() - 1); // 시작점 복귀 제거 → 열린 경로

        ProcessTerritoryRunResult result = service.process(new ProcessTerritoryRunCommand(
                100L, 500L, 900L, openPath, LocalDateTime.now().minusMinutes(30), LocalDateTime.now()));

        assertThat(result.outcome()).isEqualTo(Outcome.NO_LOOP);
        verify(territoryRepositoryPort, never()).save(any());
    }

    @Test
    void 겹치는_땅이_없으면_새_땅을_생성한다() {
        when(territoryRepositoryPort.findActiveIntersectingBboxForUpdate(any(), any(), any(), any()))
                .thenReturn(List.of());

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.CREATED);
        ArgumentCaptor<Territory> saved = ArgumentCaptor.forClass(Territory.class);
        verify(territoryRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getOwnerMemberSno()).isEqualTo(100L);
        assertThat(saved.getValue().getHp()).isEqualTo(saved.getValue().getMaxHp());
    }

    @Test
    void 남의_땅과_겹치면_데미지를_주고_기여도를_남긴다() throws Exception {
        Territory target = existingTerritory(200L, 1000, square(LAT0, LNG0));
        when(territoryRepositoryPort.findActiveIntersectingBboxForUpdate(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(target)));

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.damagedTerritorySnos()).hasSize(1);
        assertThat(result.capturedTerritories()).isEmpty();
        assertThat(target.getHp()).isLessThan(target.getMaxHp());
        verify(contributionRepositoryPort).save(any());
    }

    @Test
    void HP가_0이_되면_공격자가_점령하고_기여도를_비운다() throws Exception {
        Territory target = existingTerritory(200L, 1, square(LAT0, LNG0)); // maxHp 1 → 한 번에 소진
        when(territoryRepositoryPort.findActiveIntersectingBboxForUpdate(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(target)));
        when(contributionRepositoryPort.findByTerritorySnoAndCreatedAfter(any(), any()))
                .thenReturn(List.of());

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.capturedTerritories()).hasSize(1);
        assertThat(result.capturedTerritories().get(0).previousOwnerMemberSno()).isEqualTo(200L);
        assertThat(result.capturedTerritories().get(0).previousOwnerNickname()).isEqualTo("이전주인");
        assertThat(target.getOwnerMemberSno()).isEqualTo(100L);
        assertThat(target.getHp()).isEqualTo(target.getMaxHp()); // 리셋
        verify(contributionRepositoryPort).deleteByTerritorySno(any());
    }

    @Test
    void 내_땅과_겹치면_회복하고_기여도는_남기지_않는다() throws Exception {
        Territory mine = existingTerritory(100L, 100, square(LAT0, LNG0));
        mine.applyDamage(60); // hp 40
        when(territoryRepositoryPort.findActiveIntersectingBboxForUpdate(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(mine)));

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.healedTerritorySnos()).hasSize(1);
        assertThat(mine.getHp()).isGreaterThan(40);
        verify(contributionRepositoryPort, never()).save(any());
    }
}

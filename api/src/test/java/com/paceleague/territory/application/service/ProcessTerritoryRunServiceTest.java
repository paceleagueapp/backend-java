package com.paceleague.territory.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.season.application.port.in.shared.GetCurrentSeasonPort;
import com.paceleague.season.domain.entity.Season;
import com.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult.Outcome;
import com.paceleague.territory.application.dto.TerritoryHexOverlap;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.uber.h3core.H3Core;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// H3Core는 목(mock)이 아니라 실제 네이티브 라이브러리를 로드한 인스턴스를 쓴다 — 어떤 헥사곤이 나오는지를
// 흉내내는 것보다 실제 도형→헥사곤 변환 경로를 그대로 태우는 편이 이 테스트의 목적에 맞다(PolygonGeometry가
// 실제 JTS를 그대로 쓰는 것과 같은 이유). 기존 territory와의 "겹침"은 findActiveOverlapCounts를 목으로
// 직접 지정해 시나리오별로 만든다.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessTerritoryRunServiceTest {

    private static final double LAT0 = 37.5;
    private static final double LNG0 = 127.0;
    private static final double D = 0.002; // 약 200m — 둘레/면적 하한 통과

    @Mock
    TerritoryRepositoryPort territoryRepositoryPort;
    @Mock
    TerritoryHexRepositoryPort territoryHexRepositoryPort;
    @Mock
    GetCurrentSeasonPort getCurrentSeasonPort;
    @Mock
    GetMemberNicknamePort getMemberNicknamePort;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private H3Core h3Core;
    private ProcessTerritoryRunService service;

    @BeforeEach
    void setUp() throws Exception {
        h3Core = H3Core.newInstance();
        TerritoryProperties props = new TerritoryProperties(
                null, null, null, null, null, null, null, null);
        service = new ProcessTerritoryRunService(
                territoryRepositoryPort, territoryHexRepositoryPort,
                getCurrentSeasonPort, getMemberNicknamePort, props, h3Core, objectMapper);
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

    // 실제 겹침 판정(hex 집합 교집합)은 새 로직에서 DB 쿼리(findActiveOverlapCounts)가 담당하므로,
    // "기존 땅과 겹친 러닝"은 그 결과를 직접 목으로 지정해서 시나리오를 만든다 — 대상 territory 자체는
    // sno가 null인 순수 목 엔티티라도 무방하다(overlapBySno 키를 null로 맞춰 매칭).
    private Territory existingTerritory(Long owner) {
        return Territory.builder()
                .ownerMemberSno(owner)
                .season(1L)
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
        when(territoryHexRepositoryPort.findActiveOverlapCounts(any())).thenReturn(List.of());

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.CREATED);
        ArgumentCaptor<Territory> saved = ArgumentCaptor.forClass(Territory.class);
        verify(territoryRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getOwnerMemberSno()).isEqualTo(100L);
        assertThat(saved.getValue().getHexCount()).isGreaterThan(0);
    }

    @Test
    void 남의_땅과_조금이라도_겹치면_무조건_이번_러너가_점령한다() {
        Territory target = existingTerritory(200L);
        when(territoryHexRepositoryPort.findActiveOverlapCounts(any()))
                .thenReturn(List.of(new TerritoryHexOverlap(null, 1L)));
        when(territoryRepositoryPort.findAllByIdForUpdate(any()))
                .thenReturn(new ArrayList<>(List.of(target)));

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.capturedTerritories()).hasSize(1);
        assertThat(result.capturedTerritories().get(0).previousOwnerMemberSno()).isEqualTo(200L);
        assertThat(result.capturedTerritories().get(0).previousOwnerNickname()).isEqualTo("이전주인");
        assertThat(target.getOwnerMemberSno()).isEqualTo(100L);
        verify(territoryRepositoryPort).save(target);
    }

    @Test
    void 내_땅과_겹치면_아무_일도_일어나지_않지만_새_땅도_생기지_않는다() {
        Territory mine = existingTerritory(100L);
        when(territoryHexRepositoryPort.findActiveOverlapCounts(any()))
                .thenReturn(List.of(new TerritoryHexOverlap(null, 1L)));
        when(territoryRepositoryPort.findAllByIdForUpdate(any()))
                .thenReturn(new ArrayList<>(List.of(mine)));

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.capturedTerritories()).isEmpty();
        assertThat(mine.getOwnerMemberSno()).isEqualTo(100L);
        verify(territoryRepositoryPort, never()).save(any());
    }
}

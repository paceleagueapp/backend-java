package com.paceleague.territory.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.season.application.port.in.shared.GetCurrentSeasonPort;
import com.paceleague.season.domain.entity.Season;
import com.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult.Outcome;
import com.paceleague.territory.application.dto.TerritoryHexOwnership;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.paceleague.territory.domain.entity.TerritoryHex;
import com.paceleague.territory.domain.policy.H3TerritoryGrid;
import com.paceleague.territory.domain.policy.PolygonGeometry;
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
// 실제 JTS를 그대로 쓰는 것과 같은 이유). 기존 territory와의 "겹침"은 findActiveOwners를 목으로 직접
// 지정해 시나리오별로 만드는데, 부분 점령을 검증하려면 실제로 존재하는 헥사곤 id가 필요하므로
// coveredHexesFor(...)로 서비스와 동일한 경로(coverRing)를 태워 미리 구해둔다.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessTerritoryRunServiceTest {

    private static final double LAT0 = 37.5;
    private static final double LNG0 = 127.0;
    private static final double D = 0.002; // 약 200m — 둘레/면적 하한 통과
    private static final int RESOLUTION = 12; // TerritoryProperties 기본값

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
                null, null, null, null, null, null, null, null, null, null, null);
        service = new ProcessTerritoryRunService(
                territoryRepositoryPort, territoryHexRepositoryPort,
                getCurrentSeasonPort, getMemberNicknamePort, props, h3Core, objectMapper);
        when(getMemberNicknamePort.getNickname(any())).thenReturn("이전주인");
        // 목이라 실제 IDENTITY 채번이 없다 — sno가 아직 없는(새로 생성된) territory에는 리플렉션으로
        // 채번을 흉내내, createdTerritorySno가 null로 남는 것을 방지한다. 기존 territory(sno 이미 있음,
        // 여기서는 항상 null로 시작하는 목이라 첫 save에서 한 번만 채번됨)는 이후 save에서 그대로 유지된다.
        java.util.concurrent.atomic.AtomicLong snoSeq = new java.util.concurrent.atomic.AtomicLong(1);
        when(territoryRepositoryPort.save(any())).thenAnswer(inv -> {
            Territory t = inv.getArgument(0);
            if (t.getSno() == null) {
                java.lang.reflect.Field snoField = Territory.class.getDeclaredField("sno");
                snoField.setAccessible(true);
                snoField.set(t, snoSeq.getAndIncrement());
            }
            return t;
        });
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

    // 서비스가 실제로 계산하는 것과 동일한 경로(PolygonGeometry → H3TerritoryGrid.coverRing)로
    // 이번 러닝이 덮을 헥사곤 목록을 미리 구한다 — 부분 겹침 시나리오에서 "실제로 존재하는" 헥사곤
    // id 일부를 골라 이미 소유된 것처럼 목을 만들기 위함.
    private List<Long> coveredHexesFor(double lat, double lng) {
        PolygonGeometry polygon = PolygonGeometry.fromLatLngRing(square(lat, lng));
        return H3TerritoryGrid.coverRing(h3Core, polygon.ring(), RESOLUTION);
    }

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
        when(territoryHexRepositoryPort.findActiveOwners(any())).thenReturn(List.of());

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.CREATED);
        ArgumentCaptor<Territory> saved = ArgumentCaptor.forClass(Territory.class);
        verify(territoryRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getOwnerMemberSno()).isEqualTo(100L);
        assertThat(saved.getValue().getHexCount()).isGreaterThan(0);
    }

    @Test
    void 남의_땅과_전부_겹치면_그_땅은_사라지고_전부_이번_러너의_새_땅이_된다() {
        List<Long> covered = coveredHexesFor(LAT0, LNG0);
        Territory target = existingTerritory(200L);
        List<TerritoryHexOwnership> ownership = covered.stream()
                .map(h -> new TerritoryHexOwnership(h, target.getSno()))
                .toList();
        List<TerritoryHex> targetOwnedRows = covered.stream()
                .map(h -> TerritoryHex.of(h, target.getSno(), 1L))
                .toList();

        when(territoryHexRepositoryPort.findActiveOwners(any())).thenReturn(ownership);
        when(territoryRepositoryPort.findAllByIdForUpdate(any()))
                .thenReturn(new ArrayList<>(List.of(target)));
        when(territoryHexRepositoryPort.findByTerritorySnoIn(any())).thenReturn(targetOwnedRows);

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.capturedTerritories()).hasSize(1);
        assertThat(result.capturedTerritories().get(0).previousOwnerMemberSno()).isEqualTo(200L);
        assertThat(result.createdTerritorySno()).isNotNull(); // 뺏은 헥사곤 전부가 새 땅으로 편입됨
        verify(territoryRepositoryPort).delete(target); // 남은 헥사곤이 없으니 원래 땅은 삭제
        verify(territoryRepositoryPort, never()).save(target);

        ArgumentCaptor<Territory> savedCreated = ArgumentCaptor.forClass(Territory.class);
        verify(territoryRepositoryPort).save(savedCreated.capture());
        assertThat(savedCreated.getValue().getOwnerMemberSno()).isEqualTo(100L);
        assertThat(savedCreated.getValue().getHexCount()).isEqualTo(covered.size());
    }

    @Test
    void 남의_땅과_일부만_겹치면_겹친_헥사곤만_뺏기고_남은_헥사곤으로_원래_땅은_줄어든다() {
        List<Long> covered = coveredHexesFor(LAT0, LNG0);
        assertThat(covered.size()).isGreaterThan(2); // 시나리오 성립을 위한 전제

        // 이번 러닝이 덮은 헥사곤 중 일부(overlapping)만 target 소유, 나머지(freeCount)는 빈 헥사곤.
        int overlapCount = covered.size() / 2;
        List<Long> overlapping = covered.subList(0, overlapCount);

        Territory target = existingTerritory(200L);
        // target은 겹치는 헥사곤 외에도 이번 러닝과 무관한(멀리 떨어진) 헥사곤을 하나 더 갖고 있다 →
        // 그 몫은 안 뺏긴다. H3 라이브러리가 실제 유효한 셀만 받아들이므로 다른 위치의 진짜 헥사곤을 쓴다.
        long untouchedHex = coveredHexesFor(LAT0 + 1.0, LNG0 + 1.0).get(0);
        List<TerritoryHex> targetOwnedRows = new ArrayList<>();
        for (Long h : overlapping) {
            targetOwnedRows.add(TerritoryHex.of(h, target.getSno(), 1L));
        }
        targetOwnedRows.add(TerritoryHex.of(untouchedHex, target.getSno(), 1L));

        List<TerritoryHexOwnership> ownership = overlapping.stream()
                .map(h -> new TerritoryHexOwnership(h, target.getSno()))
                .toList();

        when(territoryHexRepositoryPort.findActiveOwners(any())).thenReturn(ownership);
        when(territoryRepositoryPort.findAllByIdForUpdate(any()))
                .thenReturn(new ArrayList<>(List.of(target)));
        when(territoryHexRepositoryPort.findByTerritorySnoIn(any())).thenReturn(targetOwnedRows);

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.capturedTerritories()).hasSize(1);
        assertThat(result.capturedTerritories().get(0).previousOwnerMemberSno()).isEqualTo(200L);
        assertThat(target.getOwnerMemberSno()).isEqualTo(200L); // 소유자 자체는 안 바뀐다 — 헥사곤만 줄어듦
        assertThat(target.getHexCount()).isEqualTo(1); // untouchedHex 하나만 남는다
        verify(territoryRepositoryPort, never()).delete(target);
        verify(territoryRepositoryPort).save(target);

        assertThat(result.createdTerritorySno()).isNotNull();
        ArgumentCaptor<Territory> savedCreated = ArgumentCaptor.forClass(Territory.class);
        verify(territoryRepositoryPort, org.mockito.Mockito.times(2)).save(savedCreated.capture());
        Territory newTerritory = savedCreated.getAllValues().stream()
                .filter(t -> t != target).findFirst().orElseThrow();
        assertThat(newTerritory.getOwnerMemberSno()).isEqualTo(100L);
        // 새 땅 = 뺏은 헥사곤(overlapCount) + 원래 빈 헥사곤(covered - overlapCount)
        assertThat(newTerritory.getHexCount()).isEqualTo(covered.size());
    }

    @Test
    void 내_땅과_겹친_부분은_그대로_두고_빈_헥사곤만_새_땅으로_편입한다() {
        List<Long> covered = coveredHexesFor(LAT0, LNG0);
        int mineCount = covered.size() / 2;
        List<Long> mineHexes = covered.subList(0, mineCount);

        Territory mine = existingTerritory(100L);
        List<TerritoryHexOwnership> ownership = mineHexes.stream()
                .map(h -> new TerritoryHexOwnership(h, mine.getSno()))
                .toList();

        when(territoryHexRepositoryPort.findActiveOwners(any())).thenReturn(ownership);
        when(territoryRepositoryPort.findAllByIdForUpdate(any()))
                .thenReturn(new ArrayList<>(List.of(mine)));

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.capturedTerritories()).isEmpty();
        assertThat(mine.getOwnerMemberSno()).isEqualTo(100L);
        verify(territoryRepositoryPort, never()).delete(any());

        assertThat(result.createdTerritorySno()).isNotNull(); // 빈 헥사곤만으로 새 땅이 생김
        ArgumentCaptor<Territory> saved = ArgumentCaptor.forClass(Territory.class);
        verify(territoryRepositoryPort).save(saved.capture()); // mine은 save 안 됨(변경 없음), 새 땅만 save
        assertThat(saved.getValue().getOwnerMemberSno()).isEqualTo(100L);
        assertThat(saved.getValue().getHexCount()).isEqualTo(covered.size() - mineCount);
    }

    @Test
    void 이번_러닝이_전부_내_땅과만_겹치면_아무_일도_일어나지_않는다() {
        List<Long> covered = coveredHexesFor(LAT0, LNG0);
        Territory mine = existingTerritory(100L);
        List<TerritoryHexOwnership> ownership = covered.stream()
                .map(h -> new TerritoryHexOwnership(h, mine.getSno()))
                .toList();

        when(territoryHexRepositoryPort.findActiveOwners(any())).thenReturn(ownership);
        when(territoryRepositoryPort.findAllByIdForUpdate(any()))
                .thenReturn(new ArrayList<>(List.of(mine)));

        ProcessTerritoryRunResult result = service.process(runOver(LAT0, LNG0));

        assertThat(result.outcome()).isEqualTo(Outcome.INTERACTED);
        assertThat(result.capturedTerritories()).isEmpty();
        assertThat(result.createdTerritorySno()).isNull();
        verify(territoryRepositoryPort, never()).save(any());
        verify(territoryRepositoryPort, never()).delete(any());
    }
}

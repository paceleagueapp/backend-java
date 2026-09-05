package com.paceleague.territory.application.service;

import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.paceleague.territory.domain.entity.TerritoryHex;
import com.paceleague.territory.domain.policy.H3TerritoryGrid;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// H3Core는 실제 네이티브 인스턴스를 쓴다(ProcessTerritoryRunServiceTest와 같은 이유).
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TerritoryHexBackfillServiceTest {

    @Mock
    TerritoryRepositoryPort territoryRepositoryPort;
    @Mock
    TerritoryHexRepositoryPort territoryHexRepositoryPort;

    private H3Core h3Core;
    private TerritoryHexBackfillService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        h3Core = H3Core.newInstance();
        TerritoryProperties props = new TerritoryProperties(
                null, null, null, null, null, null, null, null, null, null, null);
        service = new TerritoryHexBackfillService(
                territoryRepositoryPort, territoryHexRepositoryPort, props, h3Core, objectMapper);
    }

    private Territory territoryWithPolygon(Long sno, String polygonJson) {
        Territory t = Territory.builder()
                .ownerMemberSno(1L).season(1L).polygonJson(polygonJson).build();
        ReflectionTestUtils.setField(t, "sno", sno);
        return t;
    }

    private static final String SQUARE_JSON =
            "[[37.5,127.0],[37.5,127.002],[37.502,127.002],[37.502,127.0],[37.5,127.0]]";

    @Test
    void 선점된_헥사곤이_없으면_전부_백필된다() {
        Territory t = territoryWithPolygon(1L, SQUARE_JSON);
        when(territoryRepositoryPort.findBySno(1L)).thenReturn(Optional.of(t));
        when(territoryHexRepositoryPort.findExistingIndexes(any())).thenReturn(List.of());
        when(territoryRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = service.backfillOne(1L);

        assertThat(result).isTrue();
        assertThat(t.getHexCount()).isNotNull();
        assertThat(t.getHexCount()).isGreaterThan(0);
        ArgumentCaptor<List<TerritoryHex>> captor = ArgumentCaptor.forClass(List.class);
        verify(territoryHexRepositoryPort).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(t.getHexCount());
        verify(territoryRepositoryPort).save(t);
    }

    @Test
    void 덮는_헥사곤이_전부_다른_땅에_선점됐으면_백필하지_않는다() {
        Territory t = territoryWithPolygon(2L, SQUARE_JSON);
        when(territoryRepositoryPort.findBySno(2L)).thenReturn(Optional.of(t));
        List<Long> covered = H3TerritoryGrid.coverRing(h3Core, List.of(
                new double[]{37.5, 127.0}, new double[]{37.5, 127.002},
                new double[]{37.502, 127.002}, new double[]{37.502, 127.0},
                new double[]{37.5, 127.0}), 12);
        when(territoryHexRepositoryPort.findExistingIndexes(any())).thenReturn(covered);

        boolean result = service.backfillOne(2L);

        assertThat(result).isFalse();
        verify(territoryHexRepositoryPort, never()).saveAll(any());
        verify(territoryRepositoryPort, never()).save(any());
    }

    @Test
    void 존재하지_않는_땅이면_아무_일도_하지_않는다() {
        when(territoryRepositoryPort.findBySno(99L)).thenReturn(Optional.empty());

        boolean result = service.backfillOne(99L);

        assertThat(result).isFalse();
        verify(territoryHexRepositoryPort, never()).saveAll(any());
    }
}

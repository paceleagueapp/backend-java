package com.paceleague.territory.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.rank.application.port.in.shared.GetMemberTierPort;
import com.paceleague.rank.domain.enums.RankTier;
import com.paceleague.territory.application.dto.TerritoryMapQuery;
import com.paceleague.territory.application.dto.TerritoryMapResponse;
import com.paceleague.territory.application.dto.TerritoryOwnerArea;
import com.paceleague.territory.application.dto.TerritoryRankingQuery;
import com.paceleague.territory.application.dto.TerritoryRankingResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TerritoryQueryServiceTest {

    @Mock
    TerritoryRepositoryPort territoryRepositoryPort;
    @Mock
    TerritoryHexRepositoryPort territoryHexRepositoryPort;
    @Mock
    GetMemberNicknamePort getMemberNicknamePort;
    @Mock
    GetMemberTierPort getMemberTierPort;

    private H3Core h3Core;
    private TerritoryQueryService service;

    @BeforeEach
    void setUp() throws Exception {
        h3Core = H3Core.newInstance();
        TerritoryProperties props = new TerritoryProperties(
                null, null, null, null, null, null, null, null, null);
        service = new TerritoryQueryService(territoryRepositoryPort, territoryHexRepositoryPort,
                getMemberNicknamePort, getMemberTierPort, props, h3Core, new ObjectMapper());
        when(getMemberNicknamePort.getNickname(1L)).thenReturn("일등");
        when(getMemberNicknamePort.getNickname(2L)).thenReturn("이등");
        when(getMemberTierPort.getTier(any())).thenReturn(RankTier.GOLD);
    }

    private Territory territoryWithSno(Long sno, Long owner) {
        Territory t = Territory.builder().ownerMemberSno(owner).season(1L).polygonJson("[]").build();
        ReflectionTestUtils.setField(t, "sno", sno);
        return t;
    }

    @Test
    void 줌이_충분히_높지_않으면_헥사곤_경계는_비어있다() {
        Territory t = territoryWithSno(10L, 1L);
        when(territoryRepositoryPort.findActiveIntersectingBbox(any(), any(), any(), any(), anyInt()))
                .thenReturn(List.of(t));

        TerritoryMapResponse res = service.getMap(new TerritoryMapQuery(
                37.0, 127.0, 37.1, 127.1, 13, "ko", null));

        assertThat(res.territories()).hasSize(1);
        assertThat(res.territories().get(0).hexes()).isEmpty();
        verifyNoHexLookup();
    }

    @Test
    void 줌이_충분히_높으면_소유_헥사곤_경계를_함께_반환한다() {
        Territory t = territoryWithSno(10L, 1L);
        when(territoryRepositoryPort.findActiveIntersectingBbox(any(), any(), any(), any(), anyInt()))
                .thenReturn(List.of(t));
        long anyHex = H3TerritoryGrid.coverRing(h3Core, List.of(
                new double[]{37.5, 127.0}, new double[]{37.5, 127.002},
                new double[]{37.502, 127.002}, new double[]{37.502, 127.0},
                new double[]{37.5, 127.0}), 12).get(0);
        when(territoryHexRepositoryPort.findByTerritorySnoIn(List.of(10L)))
                .thenReturn(List.of(TerritoryHex.of(anyHex, 10L, 1L)));

        TerritoryMapResponse res = service.getMap(new TerritoryMapQuery(
                37.0, 127.0, 37.1, 127.1, 17, "ko", null));

        assertThat(res.territories().get(0).hexes()).hasSize(1);
    }

    private void verifyNoHexLookup() {
        verify(territoryHexRepositoryPort, never()).findByTerritorySnoIn(any());
    }

    @Test
    void 면적_내림차순으로_순위가_매겨지고_본인_항목에_mine이_표시된다() {
        when(territoryRepositoryPort.findTopOwnersByArea(anyInt())).thenReturn(List.of(
                new TerritoryOwnerArea(1L, 50_000.0, 3),
                new TerritoryOwnerArea(2L, 20_000.0, 1)));

        TerritoryRankingResponse res = service.getRanking(new TerritoryRankingQuery("ko", 2L));

        assertThat(res.entries()).hasSize(2);
        assertThat(res.entries().get(0).rank()).isEqualTo(1);
        assertThat(res.entries().get(0).nickname()).isEqualTo("일등");
        assertThat(res.entries().get(0).totalAreaSqm()).isEqualTo(50_000.0);
        assertThat(res.entries().get(0).territoryCount()).isEqualTo(3);
        assertThat(res.entries().get(0).mine()).isFalse();
        assertThat(res.entries().get(1).rank()).isEqualTo(2);
        assertThat(res.entries().get(1).mine()).isTrue();
    }

    @Test
    void 점령한_땅이_없으면_빈_랭킹을_반환한다() {
        when(territoryRepositoryPort.findTopOwnersByArea(anyInt())).thenReturn(List.of());

        TerritoryRankingResponse res = service.getRanking(new TerritoryRankingQuery("ko", null));

        assertThat(res.entries()).isEmpty();
    }
}

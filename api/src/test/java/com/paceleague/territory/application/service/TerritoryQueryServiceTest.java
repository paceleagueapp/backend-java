package com.paceleague.territory.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.rank.application.port.in.shared.GetMemberTierPort;
import com.paceleague.rank.domain.enums.RankTier;
import com.paceleague.territory.application.dto.TerritoryOwnerArea;
import com.paceleague.territory.application.dto.TerritoryRankingQuery;
import com.paceleague.territory.application.dto.TerritoryRankingResponse;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TerritoryQueryServiceTest {

    @Mock
    TerritoryRepositoryPort territoryRepositoryPort;
    @Mock
    GetMemberNicknamePort getMemberNicknamePort;
    @Mock
    GetMemberTierPort getMemberTierPort;

    private TerritoryQueryService service;

    @BeforeEach
    void setUp() {
        TerritoryProperties props = new TerritoryProperties(
                null, null, null, null, null, null, null, null, null, null, null);
        service = new TerritoryQueryService(
                territoryRepositoryPort, getMemberNicknamePort, getMemberTierPort, props, new ObjectMapper());
        when(getMemberNicknamePort.getNickname(1L)).thenReturn("일등");
        when(getMemberNicknamePort.getNickname(2L)).thenReturn("이등");
        when(getMemberTierPort.getTier(any())).thenReturn(RankTier.GOLD);
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

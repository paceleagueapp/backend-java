package com.paceleague.ranking.application.service;

import com.paceleague.crew.application.port.in.shared.GetMemberCrewBadgePort;
import com.paceleague.ranking.application.dto.AdminRankingEntry;
import com.paceleague.ranking.application.port.out.RankingProjection;
import com.paceleague.ranking.application.port.out.RankingRepositoryPort;
import com.paceleague.season.application.port.in.shared.GetCurrentSeasonPort;
import com.paceleague.season.domain.entity.Season;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RankingQueryServiceTest {

    @Mock RankingRepositoryPort rankingRepositoryPort;
    @Mock GetCurrentSeasonPort getCurrentSeasonPort;
    @Mock GetMemberCrewBadgePort getMemberCrewBadgePort;

    RankingQueryService service;

    @BeforeEach
    void setUp() {
        service = new RankingQueryService(rankingRepositoryPort, getCurrentSeasonPort, getMemberCrewBadgePort);

        Season season = new Season();
        season.setSno(7L);
        when(getCurrentSeasonPort.getCurrentSeason()).thenReturn(season);
        when(getMemberCrewBadgePort.getBadges(any())).thenReturn(Map.of());
    }

    private RankingProjection projection(Long memberSno, String nickname, int score, String tier) {
        return new RankingProjection() {
            public Long getMemberSno() { return memberSno; }
            public String getNickname() { return nickname; }
            public int getTotalScore() { return score; }
            public String getTier() { return tier; }
        };
    }

    @Test
    void 두번째_페이지의_순위는_이전_페이지_크기만큼_이어진다() {
        var pageable = PageRequest.of(1, 2);
        when(rankingRepositoryPort.findAllBySeasonSno(7L, pageable)).thenReturn(new PageImpl<>(
                List.of(projection(3L, "삼등", 1400, "SILVER")), pageable, 5));

        Page<AdminRankingEntry> result = service.getRankingPage(1, 2);

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).rank()).isEqualTo(3);
        assertThat(result.getContent().get(0).nickname()).isEqualTo("삼등");
        assertThat(result.getContent().get(0).totalScore()).isEqualTo(1400);
    }

    @Test
    void 랭킹_데이터가_없으면_빈_페이지를_반환한다() {
        var pageable = PageRequest.of(0, 20);
        when(rankingRepositoryPort.findAllBySeasonSno(7L, pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<AdminRankingEntry> result = service.getRankingPage(0, 20);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}

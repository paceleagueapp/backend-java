package com.example.paceleague.ranking.application.service;

import com.example.paceleague.rank.domain.entity.MemberScore;
import com.example.paceleague.rank.domain.enums.RankTier;
import com.example.paceleague.ranking.application.dto.RankingPageResponse;
import com.example.paceleague.ranking.application.dto.RankingUserResponse;
import com.example.paceleague.ranking.application.port.in.GetRankingUseCase;
import com.example.paceleague.ranking.application.port.out.RankingProjection;
import com.example.paceleague.ranking.application.port.out.RankingRepositoryPort;
import com.example.paceleague.season.application.port.in.GetCurrentSeasonPort;
import com.example.paceleague.season.domain.entity.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingQueryServiceImpl implements GetRankingUseCase {

    private static final int DEFAULT_SCORE = 1500;
    private static final int AROUND_LIMIT = 5;
    private static final int TOP10_LIMIT = 10;

    private final RankingRepositoryPort rankingRepositoryPort;
    private final GetCurrentSeasonPort getCurrentSeasonPort;

    public List<RankingUserResponse> getTop10() {
        // rank.RankQueryServiceImpl은 season.getSeason()을 쓰는 것과 달리 여기는 season.getSno()를 그대로 사용 —
        // 기존부터 있던 두 조회 방식의 불일치이며, 이번 리팩토링에서 의도적으로 고치지 않고 보존함.
        Season season = getCurrentSeasonPort.getCurrentSeason();
        List<RankingProjection> top10 = rankingRepositoryPort.findAroundRanking(season.getSno(), TOP10_LIMIT, 0);
        return toResponse(top10, null, 1);
    }

    public RankingPageResponse getRankingPage(Long memberSno) {
        Season season = getCurrentSeasonPort.getCurrentSeason();
        Long seasonSno = season.getSno();

        List<RankingProjection> top3 = rankingRepositoryPort.findTop3(seasonSno);

        MemberScore myScore = rankingRepositoryPort
                .findByMemberSnoAndSeasonSno(memberSno, seasonSno)
                .orElse(null);

        int myRank = myScore == null
                ? calculateDefaultRank(seasonSno)
                : calculateMyRank(seasonSno, myScore);

        int offset = Math.max(0, myRank - 3);

        List<RankingProjection> aroundRanks = rankingRepositoryPort.findAroundRanking(
                seasonSno,
                AROUND_LIMIT,
                offset
        );

        return new RankingPageResponse(
                toResponse(top3, memberSno, 1),
                toResponse(aroundRanks, memberSno, offset + 1)
        );
    }

    private int calculateMyRank(Long seasonSno, MemberScore myScore) {
        return (int) rankingRepositoryPort.countHigherRankers(
                seasonSno,
                myScore.getTotalScore(),
                myScore.getUpdateAt(),
                myScore.getMemberSno()
        ) + 1;
    }

    private int calculateDefaultRank(Long seasonSno) {
        return (int) rankingRepositoryPort
                .countBySeasonSnoAndTotalScoreGreaterThan(seasonSno, DEFAULT_SCORE) + 1;
    }

    private List<RankingUserResponse> toResponse(
            List<RankingProjection> list,
            Long myMemberSno,
            int startRank
    ) {
        List<RankingUserResponse> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            RankingProjection ranking = list.get(i);

            result.add(new RankingUserResponse(
                    startRank + i,
                    ranking.getMemberSno(),
                    ranking.getNickname(),
                    ranking.getTotalScore(),
                    RankTier.valueOf(ranking.getTier()),
                    ranking.getMemberSno().equals(myMemberSno)
            ));
        }

        return result;
    }
}

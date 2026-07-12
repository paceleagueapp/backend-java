package com.example.paceleague.ranking.service;

import com.example.paceleague.rank.entity.MemberScore;
import com.example.paceleague.rank.enums.RankTier;
import com.example.paceleague.ranking.dto.RankingPageResponse;
import com.example.paceleague.ranking.dto.RankingUserResponse;
import com.example.paceleague.ranking.repository.RankingProjection;
import com.example.paceleague.ranking.repository.RankingRepository;
import com.example.paceleague.season.entity.Season;
import com.example.paceleague.season.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingQueryService {

    private static final int DEFAULT_SCORE = 1500;
    private static final int AROUND_LIMIT = 5;
    private static final int TOP10_LIMIT = 10;

    private final RankingRepository rankingRepository;
    private final SeasonRepository seasonRepository;

    public List<RankingUserResponse> getTop10() {
        Season season = seasonRepository.findTopByOrderByStartDtDesc();
        List<RankingProjection> top10 = rankingRepository.findAroundRanking(season.getSno(), TOP10_LIMIT, 0);
        return toResponse(top10, null, 1);
    }

    public RankingPageResponse getRankingPage(Long memberSno) {
        Season season = seasonRepository.findTopByOrderByStartDtDesc();
        Long seasonSno = season.getSno();

        List<RankingProjection> top3 = rankingRepository.findTop3(seasonSno);

        MemberScore myScore = rankingRepository
                .findByMemberSnoAndSeasonSno(memberSno, seasonSno)
                .orElse(null);

        int myRank = myScore == null
                ? calculateDefaultRank(seasonSno)
                : calculateMyRank(seasonSno, myScore);

        int offset = Math.max(0, myRank - 3);

        List<RankingProjection> aroundRanks = rankingRepository.findAroundRanking(
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
        return (int) rankingRepository.countHigherRankers(
                seasonSno,
                myScore.getTotalScore(),
                myScore.getUpdateAt(),
                myScore.getMemberSno()
        ) + 1;
    }

    private int calculateDefaultRank(Long seasonSno) {
        return (int) rankingRepository
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
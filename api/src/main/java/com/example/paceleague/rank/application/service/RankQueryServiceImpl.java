package com.example.paceleague.rank.application.service;

import com.example.paceleague.rank.application.dto.RankMeResponse;
import com.example.paceleague.rank.application.port.in.GetMyRankUseCase;
import com.example.paceleague.rank.application.port.out.MemberScoreRepositoryPort;
import com.example.paceleague.rank.domain.entity.MemberScore;
import com.example.paceleague.rank.domain.enums.RankTier;
import com.example.paceleague.season.application.port.in.GetCurrentSeasonPort;
import com.example.paceleague.season.domain.entity.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankQueryServiceImpl implements GetMyRankUseCase {

    private static final int DEFAULT_SCORE = 1500;

    private final MemberScoreRepositoryPort memberScoreRepositoryPort;
    private final GetCurrentSeasonPort getCurrentSeasonPort;

    public RankMeResponse getMyRank(Long memberSno) {
        Season season = getCurrentSeasonPort.getCurrentSeason();

        // ranking 쪽(RankingQueryServiceImpl)은 season.getSno()로 조회하는 것과 달리 여기는 season.getSeason()을 그대로 사용 —
        // 기존부터 있던 두 조회 방식의 불일치이며, 이번 리팩토링에서 의도적으로 고치지 않고 보존함.
        MemberScore memberScore = memberScoreRepositoryPort
                .findByMemberSnoAndSeasonSno(memberSno, season.getSeason())
                .orElse(null);

        int totalScore;
        RankTier currentTier;

        if (memberScore == null) {
            totalScore = DEFAULT_SCORE;
            currentTier = RankTier.SILVER;
        } else {
            totalScore = memberScore.getTotalScore();
            currentTier = memberScore.getTier();
        }

        RankTier nextTier = currentTier.next();

        int nextTierRequiredScore = nextTier == null ? 0 : nextTier.getMinScore();
        int remainingScore = nextTier == null ? 0 : nextTierRequiredScore - totalScore;

        return new RankMeResponse(
                totalScore,
                currentTier,
                nextTier,
                nextTierRequiredScore,
                remainingScore
        );
    }
}

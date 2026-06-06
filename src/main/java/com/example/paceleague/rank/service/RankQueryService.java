package com.example.paceleague.rank.service;

import com.example.paceleague.rank.dto.RankMeResponse;
import com.example.paceleague.rank.entity.MemberScore;
import com.example.paceleague.rank.enums.RankTier;
import com.example.paceleague.rank.repository.MemberScoreRepository;
import com.example.paceleague.season.entity.Season;
import com.example.paceleague.season.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankQueryService {

    private static final int DEFAULT_SCORE = 1500;

    private final MemberScoreRepository memberScoreRepository;
    private final SeasonRepository seasonRepository;

    public RankMeResponse getMyRank(Long memberSno) {
        Season season = seasonRepository.findTopByOrderByStartDtDesc();

        MemberScore memberScore = memberScoreRepository
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
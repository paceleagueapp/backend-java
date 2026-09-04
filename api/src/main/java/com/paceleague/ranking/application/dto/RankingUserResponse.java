package com.paceleague.ranking.application.dto;

import com.paceleague.rank.domain.enums.RankTier;

public record RankingUserResponse(
        int rank,
        Long memberSno,
        String nickname,
        int totalScore,
        RankTier tier,
        String tierLabel,
        String crewName,
        String crewIconUrl,
        boolean me
) {
}

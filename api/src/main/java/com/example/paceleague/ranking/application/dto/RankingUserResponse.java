package com.example.paceleague.ranking.application.dto;

import com.example.paceleague.rank.domain.enums.RankTier;

public record RankingUserResponse(
        int rank,
        Long memberSno,
        String nickname,
        int totalScore,
        RankTier tier,
        String tierLabel,
        boolean me
) {
}

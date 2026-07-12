package com.example.paceleague.ranking.dto;

import com.example.paceleague.rank.enums.RankTier;

public record RankingUserResponse(
        int rank,
        Long memberSno,
        String nickname,
        int totalScore,
        RankTier tier,
        boolean me
) {
}
package com.example.paceleague.rank.application.dto;

import com.example.paceleague.rank.domain.enums.RankTier;

public record RankMeResponse(
        int totalScore,
        RankTier currentTier,
        String currentTierLabel,
        RankTier nextTier,
        String nextTierLabel,
        int nextTierRequiredScore,
        int remainingScore
) {
}

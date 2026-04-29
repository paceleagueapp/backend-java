package com.example.paceleague.rank.dto;

import com.example.paceleague.rank.enums.RankTier;

public record RankMeResponse(
        int totalScore,
        RankTier currentTier,
        RankTier nextTier,
        int nextTierRequiredScore,
        int remainingScore
) {
}
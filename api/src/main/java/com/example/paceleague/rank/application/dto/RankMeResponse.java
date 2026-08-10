package com.example.paceleague.rank.application.dto;

import com.example.paceleague.rank.domain.enums.RankTier;

public record RankMeResponse(
        int totalScore,
        RankTier currentTier,
        RankTier nextTier,
        int nextTierRequiredScore,
        int remainingScore
) {
}

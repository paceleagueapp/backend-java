package com.example.paceleague.rank.policy;

import com.example.paceleague.rank.enums.RankTier;

public class RankTierPolicy {
    public static RankTier calculate(int score) {
        RankTier result = RankTier.BRONZE;

        for (RankTier tier : RankTier.values()) {
            if (score >= tier.getMinScore()) {
                result = tier;
            }
        }

        return result;
    }
}

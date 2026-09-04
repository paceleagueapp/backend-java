package com.paceleague.rank.domain.policy;

import com.paceleague.rank.domain.enums.RankTier;

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

package com.example.paceleague.rank.policy;

import com.example.paceleague.rank.enums.RankTier;

public class RankTierPolicy {
    public static RankTier calculate(int score) {
        if (score >= 12000) return RankTier.MASTER;
        if (score >= 8000) return RankTier.DIAMOND;
        if (score >= 5000) return RankTier.PLATINUM;
        if (score >= 3000) return RankTier.GOLD;
        if (score >= 1500) return RankTier.SILVER;
        return RankTier.BRONZE;
    }
}

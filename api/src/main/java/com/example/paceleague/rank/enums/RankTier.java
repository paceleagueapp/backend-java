package com.example.paceleague.rank.enums;

import lombok.Getter;

@Getter
public enum RankTier {
    BRONZE(0),
    SILVER(1500),
    GOLD(3000),
    PLATINUM(5000),
    DIAMOND(8000),
    MASTER(12000),
    CHALLENGER(20000);

    private final int minScore;

    RankTier(int minScore) {
        this.minScore = minScore;
    }

    public RankTier next() {
        RankTier[] tiers = values();
        int nextIndex = this.ordinal() + 1;

        if (nextIndex >= tiers.length) {
            return null;
        }

        return tiers[nextIndex];
    }
}
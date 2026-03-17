package com.example.paceleague.rank.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RankScoreCalculator {
    public record RankScoreResult(
            int baseScore,
            int scaledScore,
            int addScore,
            int totalScore,
            long paceSecondsPerKm
    ) {
    }

    public static RankScoreResult calculate(
            BigDecimal distanceMeters,
            long durationSeconds,
            long weeklyRunCount
    ) {
        if (distanceMeters == null || distanceMeters.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("distance must be > 0");
        }
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("duration must be > 0");
        }

        BigDecimal distanceKm = distanceMeters.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        // 1km = 10점
        int baseScore = distanceKm.multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        long paceSecondsPerKm = BigDecimal.valueOf(durationSeconds)
                .divide(distanceKm, 0, RoundingMode.HALF_UP)
                .longValue();

        int scaledScore = 0;

        // 5:30/km 이하 = 330초 이하 → 20%
        if (paceSecondsPerKm <= 330) {
            scaledScore = BigDecimal.valueOf(baseScore)
                    .multiply(BigDecimal.valueOf(0.2))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }
        // 6:30/km 이하 = 390초 이하 → 10%
        else if (paceSecondsPerKm <= 390) {
            scaledScore = BigDecimal.valueOf(baseScore)
                    .multiply(BigDecimal.valueOf(0.1))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }

        int addScore = 0;
        if (weeklyRunCount >= 5) {
            addScore = 120;
        } else if (weeklyRunCount >= 3) {
            addScore = 50;
        }

        int totalScore = baseScore + scaledScore + addScore;

        return new RankScoreResult(baseScore, scaledScore, addScore, totalScore, paceSecondsPerKm);
    }
}

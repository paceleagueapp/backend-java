package com.paceleague.record.domain.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

// RecordServiceImpl.saveRank에 섞여 있던 순수 점수 계산 로직만 추출 — RecordSummaryCalculator와 같은 패턴.
public class RecordScoreCalculator {

    // 기본 점수 = 거리(km) * 10
    public static int calculateBaseScore(BigDecimal distanceKm) {
        return distanceKm.multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    // pace 5:30/km 이하 -> baseScore의 20%, 6:30/km 이하 -> 10%, 그 외 0
    public static int calculatePaceBonus(int baseScore, long durationSeconds, BigDecimal distanceKm) {
        long paceSecondsPerKm = BigDecimal.valueOf(durationSeconds)
                .divide(distanceKm, 0, RoundingMode.HALF_UP)
                .longValue();

        BigDecimal bonusRate;
        if (paceSecondsPerKm <= 330) {
            bonusRate = BigDecimal.valueOf(0.2);
        } else if (paceSecondsPerKm <= 390) {
            bonusRate = BigDecimal.valueOf(0.1);
        } else {
            return 0;
        }

        return BigDecimal.valueOf(baseScore)
                .multiply(bonusRate)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }
}

package com.example.paceleague.record.service;

import com.example.paceleague.record.dto.RecordSummaryDto;
import com.example.paceleague.record.dto.RecordSummaryProjection;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RecordSummaryCalculator {
    public static RecordSummaryDto from(RecordSummaryProjection projection, BigDecimal weightKg) {
        BigDecimal totalDistance = projection.getTotalDistance() == null
                ? BigDecimal.ZERO
                : projection.getTotalDistance().setScale(4, RoundingMode.HALF_UP);

        long totalDurationSeconds = projection.getTotalDurationSeconds() == null
                ? 0L
                : projection.getTotalDurationSeconds();

        long paceSecondsPerKm = 0L;
        String paceText = "0:00 /km";

        if (totalDistance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalDistanceKm = totalDistance.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

            paceSecondsPerKm = BigDecimal.valueOf(totalDurationSeconds)
                    .divide(totalDistanceKm, 0, RoundingMode.HALF_UP)
                    .longValue();

            paceText = formatPace(paceSecondsPerKm);
        }

        BigDecimal totalCalories = BigDecimal.ZERO;
        if (weightKg != null && totalDistance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalDistanceKm = totalDistance.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
            totalCalories = weightKg.multiply(totalDistanceKm).setScale(1, RoundingMode.HALF_UP);
        }

        return new RecordSummaryDto(
                totalDistance,
                totalDurationSeconds,
                paceSecondsPerKm,
                paceText,
                totalCalories
        );
    }

    private static String formatPace(long paceSecondsPerKm) {
        long minutes = paceSecondsPerKm / 60;
        long seconds = paceSecondsPerKm % 60;
        return String.format("%d:%02d /km", minutes, seconds);
    }
}

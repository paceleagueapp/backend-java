package com.example.paceleague.record.dto;

import java.math.BigDecimal;

public record RecordSummaryDto(
        BigDecimal totalDistance,
        long totalDurationSeconds,
        long paceSecondsPerKm,
        String paceText,
        BigDecimal totalCalories
) {
}

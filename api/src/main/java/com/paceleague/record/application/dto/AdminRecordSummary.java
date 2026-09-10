package com.paceleague.record.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminRecordSummary(
        Long recordSno,
        Long memberSno,
        String memberNickname,
        BigDecimal distanceMeters,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createdAt
) {}

package com.example.paceleague.record.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecordCreateRequest(
        BigDecimal distanceRecord,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String utcOffset
) {}

package com.example.paceleague.record.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecordCreateRequest(
        BigDecimal distanceRecord,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
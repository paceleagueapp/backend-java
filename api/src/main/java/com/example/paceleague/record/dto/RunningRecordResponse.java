package com.example.paceleague.record.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class RunningRecordResponse {

    private Long recordSno;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal distance;

    private LocalDateTime createAt;
}
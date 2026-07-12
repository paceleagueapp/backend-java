package com.example.paceleague.record.dto;

import java.math.BigDecimal;

public interface RecordSummaryProjection {
    BigDecimal getTotalDistance();
    Long getTotalDurationSeconds();
}

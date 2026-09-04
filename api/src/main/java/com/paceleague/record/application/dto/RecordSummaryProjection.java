package com.paceleague.record.application.dto;

import java.math.BigDecimal;

public interface RecordSummaryProjection {
    BigDecimal getTotalDistance();
    Long getTotalDurationSeconds();
}

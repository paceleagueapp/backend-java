package com.example.paceleague.record.application.dto;

import java.util.List;

public record RecordMonthResponse(
        RecordSummaryDto memberSummary,
        RecordSummaryDto monthSummary,
        List<RecordResponse> records
) {
}

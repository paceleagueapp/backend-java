package com.example.paceleague.record.dto;

import java.util.List;

public record RecordMonthResponse(
        RecordSummaryDto memberSummary,
        RecordSummaryDto monthSummary,
        List<RecordResponse> records
) {
}

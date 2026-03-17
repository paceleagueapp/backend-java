package com.example.paceleague.record.dto;

import com.example.paceleague.record.entity.Record;

import java.util.List;

public record RecordMonthResponse(
        RecordSummaryDto memberSummary,
        RecordSummaryDto monthSummary,
        List<Record> records
) {
}

package com.example.paceleague.record.application.dto;

import com.example.paceleague.record.domain.entity.Record;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecordResponse(
        Long sno,
        Long uno,
        BigDecimal distanceRecord,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
    public static RecordResponse from(Record r) {
        return new RecordResponse(
                r.getSno(),
                r.getUno(),
                r.getDistanceRecord(),
                r.getStartTime(),
                r.getEndTime(),
                r.getCreateAt(),
                r.getUpdateAt()
        );
    }
}

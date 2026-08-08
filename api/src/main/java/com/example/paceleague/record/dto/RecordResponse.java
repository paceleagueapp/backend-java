package com.example.paceleague.record.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.paceleague.record.entity.Record;

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
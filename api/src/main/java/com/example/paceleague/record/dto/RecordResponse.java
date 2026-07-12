package com.example.paceleague.record.dto;

import java.time.LocalDateTime;
import com.example.paceleague.record.entity.Record;

public record RecordResponse(
        Long sno,
        Long uno,
        Object distanceRecord, // distance 타입이 INT면 Integer, DECIMAL이면 BigDecimal로 바꿔
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
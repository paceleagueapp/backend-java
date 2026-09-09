package com.paceleague.record.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 회원 본인의 러닝 기록 1건 요약 (목록용).
// hasGpsTrack=true 이면 GET /api/record/{recordSno}/gps 로 그 러닝의 GPS 좌표 전체를 받을 수 있다.
public record RecordListItemResponse(
        Long recordSno,
        BigDecimal distanceMeters,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createAt,
        boolean hasGpsTrack
) {
}

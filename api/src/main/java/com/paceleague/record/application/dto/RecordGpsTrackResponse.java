package com.paceleague.record.application.dto;

import com.paceleague.record.application.dto.GpsSessionRequest.GpsPoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 러닝 1건의 GPS 트랙 전체. record_track.points_json 을 파싱한 좌표 배열 + 트랙 메타.
// 좌표 1건의 형태는 앱이 올린 것과 동일(GpsSessionRequest.GpsPoint):
//   sequence, recordedAt(UTC), latitude, longitude, altitudeMeters, accuracyMeters, rawLatitude, rawLongitude
public record RecordGpsTrackResponse(
        Long recordSno,
        Long trackSno,
        String status,            // FINISHED / ABANDONED
        boolean territoryMode,    // 땅따먹기 모드로 뛴 러닝이었는지
        LocalDateTime startedAt,  // 첫 좌표 시각(UTC)
        LocalDateTime endedAt,    // 마지막 좌표 시각(UTC)
        BigDecimal distanceMeters,
        int pointCount,
        List<GpsPoint> points
) {
}

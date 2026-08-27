package com.example.paceleague.record.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// 앱이 러닝 종료 시 보내는 GPS 세션 전체 페이로드.
// 타임스탬프는 앱에서 ISO-8601(UTC, 예: "2026-08-26T10:46:25.797Z")로 보내므로 OffsetDateTime으로 받아
// 서비스에서 UTC LocalDateTime으로 변환해 record에 저장한다(기존 RecordCreateRequest는 LocalDateTime).
public record GpsSessionRequest(
        Integer schemaVersion,
        String clientRunId,
        String activityType,
        String status,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        Long elapsedDurationMs,
        BigDecimal distanceMeters,
        Integer pointCount,
        Location location,
        Device device,
        List<GpsPoint> points,
        // 페이로드에 오프셋 정보가 없어 앱이 별도로 넣어주면 그대로 record.utc_offset에 저장(선택, 예: "+09:00").
        String utcOffset
) {
    public record Location(
            Integer requestedIntervalMs,
            BigDecimal distanceFilterMeters,
            String algorithmVersion
    ) {}

    public record Device(
            String platform,
            String appVersion,
            Integer appBuildNumber
    ) {}

    public record GpsPoint(
            Integer sequence,
            OffsetDateTime recordedAt,
            Double latitude,
            Double longitude,
            Double altitudeMeters,
            Double accuracyMeters,
            Double rawLatitude,
            Double rawLongitude
    ) {}
}

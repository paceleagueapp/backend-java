package com.example.paceleague.record.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// 앱이 러닝 중 5분마다 보내는 GPS 청크. 같은 러닝은 clientRunId로 묶이고, 마지막 청크에 finished=true가 실린다.
// 타임스탬프는 앱에서 ISO-8601(UTC, 예: "2026-08-26T10:46:25.797Z")로 오므로 OffsetDateTime으로 받아
// 서비스에서 UTC LocalDateTime으로 변환한다.
public record GpsSessionRequest(
        String clientRunId,
        // 선택. 없으면 RUNNING으로 간주. RUNNING 외 값은 거부.
        String activityType,
        // 이번 5분 청크 동안 수집된 좌표들. finished=true 이면서 더 보낼 좌표가 없으면 비어 있어도 된다.
        List<GpsPoint> points,
        // 마지막 청크에서 true. 이때 record 1건 생성 + 점수 산정이 일어난다. 없으면 false.
        Boolean finished,
        // 선택. 보통 첫 청크에만 채워 보냄.
        Location location,
        Device device,
        Integer schemaVersion,
        // 선택. 앱이 넣어주면 그대로 record.utc_offset에 저장(예: "+09:00").
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

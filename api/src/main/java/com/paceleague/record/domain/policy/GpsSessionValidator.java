package com.paceleague.record.domain.policy;

import com.paceleague.record.application.dto.GpsSessionRequest;

import java.util.List;

// GPS 청크 페이로드 자체의 형식 검증. 거리/페이스 상한 등 러닝 기록으로서의 검증은
// 종료(finished) 시 RecordService.validateRequest가 이어서 수행한다.
public final class GpsSessionValidator {

    // 한 청크(5분)에 담길 수 있는 좌표 수의 보수적 상한 — 1초 간격이어도 300개면 충분.
    public static final int MAX_CHUNK_POINTS = 2_000;
    // 한 러닝 전체 좌표 수 상한 — 3초 간격 기준 50시간 분량. 이보다 크면 조작된 세션으로 간주.
    public static final int MAX_SESSION_POINTS = 60_000;

    private GpsSessionValidator() {}

    public static void validate(Long uno, GpsSessionRequest req) {
        if (uno == null || uno <= 0) {
            throw new IllegalArgumentException("uno is invalid");
        }
        if (req == null) {
            throw new IllegalArgumentException("body is required");
        }
        if (isBlank(req.clientRunId())) {
            throw new IllegalArgumentException("clientRunId is required");
        }
        if (req.clientRunId().length() > 100) {
            throw new IllegalArgumentException("clientRunId is too long (max 100)");
        }
        if (req.activityType() != null && !"RUNNING".equalsIgnoreCase(req.activityType())) {
            throw new IllegalArgumentException("unsupported activityType: " + req.activityType());
        }

        List<GpsSessionRequest.GpsPoint> points = req.points();
        boolean finished = Boolean.TRUE.equals(req.finished());

        if (points == null || points.isEmpty()) {
            if (!finished) {
                throw new IllegalArgumentException("points is empty");
            }
            return; // finished=true 이면서 마저 보낼 좌표가 없는 마감 요청은 허용
        }
        if (points.size() > MAX_CHUNK_POINTS) {
            throw new IllegalArgumentException("too many points in one chunk (max " + MAX_CHUNK_POINTS + ")");
        }
        for (GpsSessionRequest.GpsPoint p : points) {
            if (p.recordedAt() == null) {
                throw new IllegalArgumentException("point recordedAt is required");
            }
            if (p.latitude() == null || p.longitude() == null) {
                throw new IllegalArgumentException("point latitude/longitude is required");
            }
            if (p.latitude() < -90 || p.latitude() > 90) {
                throw new IllegalArgumentException("point latitude out of range: " + p.latitude());
            }
            if (p.longitude() < -180 || p.longitude() > 180) {
                throw new IllegalArgumentException("point longitude out of range: " + p.longitude());
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

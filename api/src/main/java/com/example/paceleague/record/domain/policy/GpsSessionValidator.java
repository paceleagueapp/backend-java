package com.example.paceleague.record.domain.policy;

import com.example.paceleague.record.application.dto.GpsSessionRequest;

import java.math.BigDecimal;
import java.util.List;

// GPS 세션 페이로드 자체의 형식 검증만 담당하는 순수 로직.
// 거리/페이스 상한 등 러닝 기록으로서의 검증은 RecordServiceImpl.validateRequest가 이어서 수행한다.
public final class GpsSessionValidator {

    // 3초 간격 수집 기준 하루를 넘겨도 남을 만큼의 보수적 상한 — 이보다 많으면 조작된 페이로드로 간주.
    public static final int MAX_POINTS = 100_000;

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
        if (req.schemaVersion() == null) {
            throw new IllegalArgumentException("schemaVersion is required");
        }
        if (!"RUNNING".equalsIgnoreCase(nullToEmpty(req.activityType()))) {
            throw new IllegalArgumentException("unsupported activityType: " + req.activityType());
        }
        if (!"FINISHED".equalsIgnoreCase(nullToEmpty(req.status()))) {
            throw new IllegalArgumentException("only FINISHED sessions can be saved");
        }
        if (req.startedAt() == null || req.endedAt() == null) {
            throw new IllegalArgumentException("startedAt and endedAt are required");
        }
        if (!req.endedAt().isAfter(req.startedAt())) {
            throw new IllegalArgumentException("endedAt must be after startedAt");
        }
        if (req.distanceMeters() == null || req.distanceMeters().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("distanceMeters must be positive");
        }

        List<GpsSessionRequest.GpsPoint> points = req.points();
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("points is empty");
        }
        if (points.size() > MAX_POINTS) {
            throw new IllegalArgumentException("too many points (max " + MAX_POINTS + ")");
        }
        for (GpsSessionRequest.GpsPoint p : points) {
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

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}

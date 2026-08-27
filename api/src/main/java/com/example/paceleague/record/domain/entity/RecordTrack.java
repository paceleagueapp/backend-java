package com.example.paceleague.record.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 러닝 세션의 GPS 트랙(경로) 원본. record 1건당 최대 1행이며 record_sno로 record와 느슨하게 연결된다(FK 미강제).
// 좌표 배열은 컬럼으로 쪼개지 않고 points_json(LONGTEXT)에 JSON 통째로 저장 — 지도에 경로를 그리는 용도로 충분.
// 필드가 많아 Record처럼 위치 인자 생성자 대신 Lombok @Builder를 쓴다(매핑은 서비스 계층에서 수행).
@Entity
@Table(name = "record_track")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "uno", nullable = false)
    private Long uno;

    @Column(name = "record_sno", nullable = false)
    private Long recordSno;

    @Column(name = "client_run_id", nullable = false, length = 100)
    private String clientRunId;

    @Column(name = "schema_version")
    private Integer schemaVersion;

    @Column(name = "activity_type", length = 30)
    private String activityType;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "elapsed_duration_ms")
    private Long elapsedDurationMs;

    @Column(name = "distance_meters")
    private BigDecimal distanceMeters;

    @Column(name = "point_count")
    private Integer pointCount;

    @Column(name = "loc_requested_interval_ms")
    private Integer locRequestedIntervalMs;

    @Column(name = "loc_distance_filter_meters")
    private BigDecimal locDistanceFilterMeters;

    @Column(name = "loc_algorithm_version", length = 100)
    private String locAlgorithmVersion;

    @Column(name = "device_platform", length = 20)
    private String devicePlatform;

    @Column(name = "device_app_version", length = 30)
    private String deviceAppVersion;

    @Column(name = "device_app_build_number")
    private Integer deviceAppBuildNumber;

    @Column(name = "points_json", columnDefinition = "LONGTEXT")
    private String pointsJson;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Builder
    private RecordTrack(Long uno, Long recordSno, String clientRunId, Integer schemaVersion,
                        String activityType, String status, LocalDateTime startedAt, LocalDateTime endedAt,
                        Long elapsedDurationMs, BigDecimal distanceMeters, Integer pointCount,
                        Integer locRequestedIntervalMs, BigDecimal locDistanceFilterMeters, String locAlgorithmVersion,
                        String devicePlatform, String deviceAppVersion, Integer deviceAppBuildNumber,
                        String pointsJson) {
        this.uno = uno;
        this.recordSno = recordSno;
        this.clientRunId = clientRunId;
        this.schemaVersion = schemaVersion;
        this.activityType = activityType;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.elapsedDurationMs = elapsedDurationMs;
        this.distanceMeters = distanceMeters;
        this.pointCount = pointCount;
        this.locRequestedIntervalMs = locRequestedIntervalMs;
        this.locDistanceFilterMeters = locDistanceFilterMeters;
        this.locAlgorithmVersion = locAlgorithmVersion;
        this.devicePlatform = devicePlatform;
        this.deviceAppVersion = deviceAppVersion;
        this.deviceAppBuildNumber = deviceAppBuildNumber;
        this.pointsJson = pointsJson;
        this.createAt = LocalDateTime.now();
    }
}

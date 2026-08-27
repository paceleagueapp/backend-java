package com.example.paceleague.record.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 러닝 세션의 GPS 트랙. 앱이 5분마다 보내는 청크를 여기에 누적한다(러닝 1건 = 1행).
//  - status=ACTIVE: 진행 중. points_json에 좌표가 계속 append되고 record_sno는 null.
//  - status=FINISHED: 앱이 finished=true를 보낸 시점에 record 1건이 생성되고 record_sno가 채워짐.
// 좌표 배열은 컬럼으로 쪼개지 않고 points_json(LONGTEXT)에 JSON 통째로 저장한다(지도 경로 표시 용도).
@Entity
@Table(name = "record_track")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordTrack {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_FINISHED = "FINISHED";
    // 앱이 finished=true를 못 보낸 채 끊겼고, 쌓인 좌표로도 정상 기록을 만들 수 없어 스위퍼가 폐기한 세션.
    public static final String STATUS_ABANDONED = "ABANDONED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "uno", nullable = false)
    private Long uno;

    // 종료 전에는 null, finished 처리 시점에 채워짐.
    @Column(name = "record_sno")
    private Long recordSno;

    @Column(name = "client_run_id", nullable = false, length = 100)
    private String clientRunId;

    @Column(name = "schema_version")
    private Integer schemaVersion;

    @Column(name = "activity_type", length = 30)
    private String activityType;

    // 러닝 시작 시 "땅따먹기 모드"로 시작한 세션이면 true. 세션 생성 시점에 확정되어 이후 불변이며,
    // FINISHED 처리 시 이 값이 true인 세션만 territory(땅따먹기) 판정 대상이 된다.
    @Column(name = "territory_mode", nullable = false)
    private boolean territoryMode;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 마지막으로 저장된 좌표의 시각 — 다음 청크에서 이보다 이후 좌표만 받는다(재전송 중복 방지 워터마크).
    @Column(name = "last_point_at")
    private LocalDateTime lastPointAt;

    // 마지막으로 저장된 좌표의 위경도 — 다음 청크 첫 좌표와의 거리를 이어 붙이기 위해 보관.
    @Column(name = "last_lat")
    private BigDecimal lastLat;

    @Column(name = "last_lng")
    private BigDecimal lastLng;

    @Column(name = "elapsed_duration_ms")
    private Long elapsedDurationMs;

    @Column(name = "distance_meters")
    private BigDecimal distanceMeters;

    @Column(name = "point_count")
    private Integer pointCount;

    @Column(name = "chunk_count")
    private Integer chunkCount;

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

    @Column(name = "utc_offset", length = 50)
    private String utcOffset;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Builder
    private RecordTrack(Long uno, String clientRunId, String activityType, boolean territoryMode, Integer schemaVersion,
                        String utcOffset, LocalDateTime startedAt,
                        Integer locRequestedIntervalMs, BigDecimal locDistanceFilterMeters, String locAlgorithmVersion,
                        String devicePlatform, String deviceAppVersion, Integer deviceAppBuildNumber) {
        this.uno = uno;
        this.clientRunId = clientRunId;
        this.activityType = activityType;
        this.territoryMode = territoryMode;
        this.schemaVersion = schemaVersion;
        this.utcOffset = utcOffset;
        this.status = STATUS_ACTIVE;
        this.startedAt = startedAt;
        this.endedAt = startedAt;
        this.distanceMeters = BigDecimal.ZERO;
        this.pointCount = 0;
        this.chunkCount = 0;
        this.pointsJson = "[]";
        this.locRequestedIntervalMs = locRequestedIntervalMs;
        this.locDistanceFilterMeters = locDistanceFilterMeters;
        this.locAlgorithmVersion = locAlgorithmVersion;
        this.devicePlatform = devicePlatform;
        this.deviceAppVersion = deviceAppVersion;
        this.deviceAppBuildNumber = deviceAppBuildNumber;
        this.createAt = LocalDateTime.now();
        this.updateAt = this.createAt;
    }

    // 이번 청크에서 새로 저장된 좌표들을 누적 반영한다.
    public void appendChunk(int addedPoints, LocalDateTime lastPointAt, BigDecimal lastLat, BigDecimal lastLng,
                            BigDecimal addedDistanceMeters, String mergedPointsJson) {
        this.pointCount += addedPoints;
        this.chunkCount += 1;
        this.lastPointAt = lastPointAt;
        this.endedAt = lastPointAt;
        this.lastLat = lastLat;
        this.lastLng = lastLng;
        this.distanceMeters = this.distanceMeters.add(addedDistanceMeters);
        this.pointsJson = mergedPointsJson;
        this.updateAt = LocalDateTime.now();
    }

    // finished=true 청크를 받아 record가 만들어진 뒤 호출.
    public void finish(Long recordSno) {
        this.recordSno = recordSno;
        this.status = STATUS_FINISHED;
        if (this.startedAt != null && this.endedAt != null) {
            this.elapsedDurationMs = java.time.Duration.between(this.startedAt, this.endedAt).toMillis();
        }
        this.updateAt = LocalDateTime.now();
    }

    // 스위퍼가 자동 마감을 시도했지만 유효한 러닝 기록으로 만들 수 없을 때(좌표 없음, 거리/페이스 비정상 등).
    public void abandon() {
        this.status = STATUS_ABANDONED;
        this.updateAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(this.status);
    }

    public boolean isFinished() {
        return STATUS_FINISHED.equals(this.status);
    }
}

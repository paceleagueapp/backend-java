-- 러닝 세션의 GPS 트랙(경로) 저장 기능을 위한 수동 마이그레이션.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행해야 합니다.
-- 로컬 개발 환경은 ddl-auto: update가 테이블을 자동 생성하므로 이 파일을 실행할 필요가 없습니다.

CREATE TABLE record_track (
    sno                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    uno                        BIGINT        NOT NULL,
    record_sno                 BIGINT        NOT NULL,
    client_run_id              VARCHAR(100)  NOT NULL,
    schema_version             INT           NULL,
    activity_type              VARCHAR(30)   NULL,
    status                     VARCHAR(20)   NULL,
    started_at                 DATETIME      NULL,
    ended_at                   DATETIME      NULL,
    elapsed_duration_ms        BIGINT        NULL,
    distance_meters            DECIMAL(12,4) NULL,
    point_count                INT           NULL,
    loc_requested_interval_ms  INT           NULL,
    loc_distance_filter_meters DECIMAL(8,3)  NULL,
    loc_algorithm_version      VARCHAR(100)  NULL,
    device_platform            VARCHAR(20)   NULL,
    device_app_version         VARCHAR(30)   NULL,
    device_app_build_number    INT           NULL,
    points_json                LONGTEXT      NULL,
    create_at                  DATETIME      NULL,
    UNIQUE KEY uk_record_track_client_run_id (client_run_id),
    KEY idx_record_track_uno (uno),
    KEY idx_record_track_record_sno (record_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

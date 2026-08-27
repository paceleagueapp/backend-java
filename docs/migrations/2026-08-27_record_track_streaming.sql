-- record_track을 "5분마다 들어오는 GPS 청크를 누적하는 세션 테이블"로 확장하는 마이그레이션.
-- 같은 날 먼저 적용한 2026-08-27_record_gps_track.sql(테이블 최초 생성) 다음에 실행합니다.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행해야 합니다.
-- 로컬 개발 환경은 ddl-auto: update가 자동 반영하므로 실행할 필요가 없습니다.
--
-- record_track 최초 설계(세션 종료 시 한 번에 업로드)를 앱의 실제 방식(러닝 중 5분마다 좌표 청크 전송)에
-- 맞춰 바꾼 것입니다. record_sno는 종료 전에는 비어 있어야 하므로 NULL 허용으로 바꾸고,
-- 청크 누적에 필요한 워터마크/직전 좌표/청크 수 컬럼을 추가합니다.

ALTER TABLE record_track
    MODIFY COLUMN record_sno BIGINT NULL,
    ADD COLUMN last_point_at DATETIME     NULL AFTER ended_at,
    ADD COLUMN last_lat       DECIMAL(10,7) NULL AFTER last_point_at,
    ADD COLUMN last_lng       DECIMAL(10,7) NULL AFTER last_lat,
    ADD COLUMN chunk_count    INT          NULL AFTER point_count,
    ADD COLUMN utc_offset     VARCHAR(50)  NULL AFTER points_json,
    ADD COLUMN update_at      DATETIME     NULL AFTER create_at;

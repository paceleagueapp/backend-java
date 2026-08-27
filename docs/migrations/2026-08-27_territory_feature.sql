-- 러닝 땅따먹기(territory) 1차 기능을 위한 수동 마이그레이션.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL/MariaDB에 직접 실행해야 합니다.
-- 로컬 개발 환경은 ddl-auto: update가 테이블/컬럼을 자동 생성하므로 이 파일을 실행할 필요가 없습니다.
--
-- 같은 날 먼저 적용한 record_track 관련 마이그레이션들 다음에 실행하세요.
--   2026-08-27_record_gps_track.sql -> 2026-08-27_record_track_streaming.sql -> (이 파일)

-- 1) 러닝 시작 시 "땅따먹기 모드"로 시작한 세션 표시. 없으면 0(일반 러닝).
ALTER TABLE record_track
    ADD COLUMN territory_mode TINYINT(1) NOT NULL DEFAULT 0 AFTER activity_type;

-- 2) 땅 1구획. 러닝 GPS 경로가 이룬 닫힌 도형 하나 = territory 한 행.
CREATE TABLE territory (
    sno               BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_member_sno  BIGINT         NOT NULL,
    season            BIGINT         NULL,
    polygon_json      LONGTEXT       NULL,          -- [[lat,lng], ...] 위/경도 링(지도에 그대로 그림)
    bbox_min_lat      DECIMAL(10,7)  NULL,
    bbox_min_lng      DECIMAL(10,7)  NULL,
    bbox_max_lat      DECIMAL(10,7)  NULL,
    bbox_max_lng      DECIMAL(10,7)  NULL,
    center_lat        DECIMAL(10,7)  NULL,
    center_lng        DECIMAL(10,7)  NULL,
    area_sqm          DECIMAL(18,4)  NULL,
    perimeter_m       DECIMAL(14,4)  NULL,
    hp                INT            NOT NULL,
    max_hp            INT            NOT NULL,
    source_record_sno BIGINT         NULL,
    source_track_sno  BIGINT         NULL,
    status            VARCHAR(20)    NULL,          -- ACTIVE
    create_at         DATETIME       NULL,
    update_at         DATETIME       NULL,
    KEY idx_territory_bbox (bbox_min_lat, bbox_max_lat, bbox_min_lng, bbox_max_lng),
    KEY idx_territory_owner (owner_member_sno),
    KEY idx_territory_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) 한 땅에 대한 공격 기여도 로그(러닝 1회 = 최대 1건). HP 0 시점에 1시간 윈도우로 합산해 점령자를 정하고,
--    점령이 확정되면 해당 territory의 행은 모두 삭제되어 새로 시작한다.
CREATE TABLE territory_contribution (
    sno          BIGINT AUTO_INCREMENT PRIMARY KEY,
    territory_sno BIGINT   NOT NULL,
    member_sno   BIGINT    NOT NULL,
    damage       INT       NOT NULL,
    create_at    DATETIME  NULL,
    KEY idx_tc_territory_time (territory_sno, create_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

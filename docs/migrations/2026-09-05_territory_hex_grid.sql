-- 땅따먹기(territory) 소유권 판정을 폴리곤 교집합(JTS)에서 H3 헥사곤 격자(resolution 12) 기반으로
-- 전환하기 위한 수동 마이그레이션. 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접
-- 실행해야 합니다. 로컬 개발 환경은 ddl-auto: update가 자동 생성하므로 실행할 필요 없습니다.
--
-- 2026-08-27_territory_feature.sql 다음에 실행하세요.

-- 1) 땅 1구획을 이루는 H3 헥사곤 개수. 생성 시 고정, 점령(capture)으로도 바뀌지 않는다.
ALTER TABLE territory
    ADD COLUMN hex_count INT NULL AFTER area_sqm;

-- 2) 헥사곤 1개 = 어느 territory에 속하는지 매핑. h3_index를 PK로 둬서 "한 헥사곤은 동시에 하나의
--    ACTIVE territory에만 속한다"는 불변식을 DB 유니크 제약으로도 보장한다. 소유자/HP는 territory
--    테이블에만 있고(단위 유지) 점령이 일어나도 이 행의 territory_sno는 절대 바뀌지 않는다.
CREATE TABLE territory_hex (
    h3_index      BIGINT   NOT NULL PRIMARY KEY,
    territory_sno BIGINT   NOT NULL,
    season        BIGINT   NULL,
    create_at     DATETIME NULL,
    KEY idx_th_territory (territory_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 주의: 이 마이그레이션은 스키마만 만든다. 2026-08-27 이후 이미 생성된 기존 ACTIVE territory 행들은
-- territory_hex에 대응하는 헥사곤 행이 하나도 없는 상태로 남는다 — 배포 이후에는 겹침 판정이 전부
-- territory_hex 기준으로 이루어지므로, 그 땅들은 지도에는 계속 보이지만 새로운 러닝과 더 이상
-- 상호작용(공격/회복/점령)하지 않는 "유령 땅"이 된다. 운영에 이미 생긴 ACTIVE 땅이 있다면, 배포 전에
-- 그 polygon_json들을 H3TerritoryGrid.coverRing(resolution=12)로 변환해 territory_hex를 채우고
-- hex_count/area_sqm을 갱신하는 1회성 백필이 별도로 필요합니다(이 마이그레이션에는 포함하지 않음 —
-- H3 인코딩은 SQL만으로 할 수 없어 애플리케이션 코드로 돌려야 함).

-- 땅따먹기 HP/공격력/회복력/기여도 시스템 제거. 겹치면 무조건 그 러너의 소유로 즉시 바뀐다(HP 소모 없음).
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행해야 합니다.
-- 로컬 개발 환경은 ddl-auto: update가 컬럼 삭제까지는 자동으로 안 해주므로, 로컬도 이 파일을 실행하세요.
--
-- 2026-09-05_territory_hex_grid.sql 다음에 실행하세요.

ALTER TABLE territory
    DROP COLUMN hp,
    DROP COLUMN max_hp;

DROP TABLE territory_contribution;

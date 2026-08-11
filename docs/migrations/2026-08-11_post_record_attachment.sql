-- 게시글 작성 시 본인 러닝기록을 첨부하는 기능을 위한 수동 마이그레이션.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행해야 합니다.
-- 로컬 개발 환경은 ddl-auto: update가 컬럼을 자동 추가하므로 이 파일을 실행할 필요가 없습니다.

ALTER TABLE post
    ADD COLUMN record_sno BIGINT NULL AFTER member_sno;

-- 회원 탈퇴(soft delete + 즉시 개인정보 삭제) 지원.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행. 로컬(ddl-auto: update)은 자동.

ALTER TABLE member
    ADD COLUMN status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER email,   -- ACTIVE / WITHDRAWN
    ADD COLUMN withdrawn_at DATETIME    NULL                       AFTER status;

CREATE INDEX idx_member_status ON member (status);

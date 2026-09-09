-- 게시판 신고(자동 숨김) + 회원 차단.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행. 로컬(ddl-auto: update)은 자동.

-- 1) 신고
CREATE TABLE board_report (
    sno                 BIGINT       NOT NULL AUTO_INCREMENT,
    reporter_member_sno BIGINT       NOT NULL,
    target_type         VARCHAR(10)  NOT NULL,           -- POST / COMMENT
    target_sno          BIGINT       NOT NULL,
    reason              VARCHAR(20)  NOT NULL,           -- SPAM / ABUSE / SEXUAL / ETC
    detail              VARCHAR(500) NULL,
    status              VARCHAR(20)  NOT NULL,           -- OPEN / RESOLVED / DISMISSED
    created_at          DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_report_reporter_target (reporter_member_sno, target_type, target_sno),
    KEY idx_report_target (target_type, target_sno, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) 자동 숨김 플래그
ALTER TABLE post
    ADD COLUMN hidden    TINYINT(1) NOT NULL DEFAULT 0 AFTER score,
    ADD COLUMN hidden_at DATETIME   NULL              AFTER hidden;

ALTER TABLE comment
    ADD COLUMN hidden    TINYINT(1) NOT NULL DEFAULT 0 AFTER score,
    ADD COLUMN hidden_at DATETIME   NULL              AFTER hidden;

-- 3) 회원 차단 (단방향)
CREATE TABLE member_block (
    sno                BIGINT   NOT NULL AUTO_INCREMENT,
    blocker_member_sno BIGINT   NOT NULL,
    blocked_member_sno BIGINT   NOT NULL,
    created_at         DATETIME NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_block (blocker_member_sno, blocked_member_sno),
    KEY idx_block_blocker (blocker_member_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

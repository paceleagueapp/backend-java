-- 전체 발송 푸시(FCM 토픽) 1건의 이력 + "하루 1회" 중복 발송 가드.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행해야 합니다.
-- 로컬(ddl-auto: update)은 자동 생성되므로 실행 불필요.
--
-- notification 도메인 신규(2026-09-09) — 매일 09:00(KST) "어제 N명이 M개의 땅 점령" 요약 푸시.

CREATE TABLE push_send_log (
    sno            BIGINT       NOT NULL AUTO_INCREMENT,
    kind           VARCHAR(40)  NOT NULL,          -- 'DAILY_TERRITORY_DIGEST'
    target_date    DATE         NOT NULL,          -- 집계 대상 '전날'(KST)
    title          VARCHAR(200) NULL,
    body           VARCHAR(500) NULL,
    payload_json   VARCHAR(1000) NULL,
    fcm_message_id VARCHAR(200) NULL,              -- 토픽 발송 성공 시 messageId
    status         VARCHAR(20)  NOT NULL,          -- RESERVED / SENT / SKIPPED / FAILED
    detail         VARCHAR(500) NULL,              -- 스킵/실패 사유
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_push_send_log_kind_date (kind, target_date)   -- 하루 1건 = 중복 발송 가드
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

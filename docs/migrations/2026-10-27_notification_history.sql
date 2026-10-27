-- 알림내역(인박스). 실제로 발송된 푸시 1건 = 1행. 웹(/notifications)·앱(GET /api/notification)에서 조회.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL(paceleague-db)에 직접 실행해야 합니다.
-- 로컬(ddl-auto: update)은 자동 생성되므로 실행 불필요.
--
-- member_sno NULL = 전체 발송(FCM 토픽 브로드캐스트, 예: 매일 아침 땅따먹기 요약) — 모든 회원의 내역에 보이되
-- 가입 이후 발송분만 보인다(조회 쿼리에서 created_at >= 가입시각). member_sno 값이 있으면 그 회원 개인 알림.

CREATE TABLE notification_history (
    sno         BIGINT       NOT NULL AUTO_INCREMENT,
    member_sno  BIGINT       NULL,                 -- NULL = 전체 발송
    kind        VARCHAR(40)  NOT NULL,             -- 'DAILY_TERRITORY_DIGEST' 등
    title       VARCHAR(200) NOT NULL,
    body        VARCHAR(500) NOT NULL,
    deeplink    VARCHAR(300) NULL,                 -- 앱 딥링크 (예: paceleague://territory)
    created_at  DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    KEY idx_notification_history_member (member_sno, sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

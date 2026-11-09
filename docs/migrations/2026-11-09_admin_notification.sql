-- 관리자 알림관리(/admin/notifications) — 관리자가 회원을 골라 보낸 푸시 알림과 받는 회원.
-- content 는 게시글 에디터 HTML(서버에서 정제된 값). 앱은 푸시 딥링크 paceleague://notifications/{sno} 로
-- GET /api/notification/notice/{sno} 를 불러 본문을 보여준다(받는 회원만 조회 가능).
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL(paceleague-db)에 직접 실행해야 합니다.
-- 로컬(ddl-auto: update)은 자동 생성되므로 실행 불필요.

CREATE TABLE admin_notification (
    sno                  BIGINT       NOT NULL AUTO_INCREMENT,
    title                VARCHAR(200) NOT NULL,
    content              TEXT         NOT NULL,
    target_count         INT          NOT NULL,
    sent_count           INT          NOT NULL,  -- 실제로 1대 이상 발송된 회원 수
    created_by_admin_sno BIGINT       NOT NULL,
    created_at           DATETIME     NOT NULL,
    PRIMARY KEY (sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_notification_target (
    sno              BIGINT     NOT NULL AUTO_INCREMENT,
    notification_sno BIGINT     NOT NULL,
    member_sno       BIGINT     NOT NULL,
    sent             TINYINT(1) NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uk_admin_notification_target (notification_sno, member_sno),
    KEY idx_admin_notification_target_member (member_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

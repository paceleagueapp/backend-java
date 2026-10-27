-- 관리자 팝업관리(/admin/popups) — 공지 팝업. 공개 조회 GET /api/popup 은 active=1 이고
-- start_at <= 지금(KST) < end_at 인 행만 내려준다. start_at/end_at 은 관리자가 입력한 한국시간 그대로 저장.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL(paceleague-db)에 직접 실행해야 합니다.
-- 로컬(ddl-auto: update)은 자동 생성되므로 실행 불필요.

CREATE TABLE popup (
    sno        BIGINT       NOT NULL AUTO_INCREMENT,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,            -- 게시글 에디터 HTML(서버에서 정제된 값)
    target     VARCHAR(10)  NOT NULL,            -- WEB / APP / BOTH
    start_at   DATETIME     NOT NULL,            -- KST
    end_at     DATETIME     NOT NULL,            -- KST (미포함)
    active     TINYINT(1)   NOT NULL,
    create_at  DATETIME     NOT NULL,
    update_at  DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    KEY idx_popup_active_period (active, start_at, end_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

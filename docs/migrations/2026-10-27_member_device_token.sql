-- 회원별 푸시 기기 토큰(FCM registration token). 매일 아침 땅따먹기 요약 등 "전체 회원" 발송을
-- FCM 토픽(all — 비회원 설치자 포함) 대신 이 테이블의 토큰으로 보낸다.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL(paceleague-db)에 직접 실행해야 합니다.
-- 로컬(ddl-auto: update)은 자동 생성되므로 실행 불필요.
--
-- 앱이 로그인 직후/토큰 갱신 시 POST /api/notification/device-token 으로 등록(token 기준 upsert — 같은 기기에서
-- 다른 계정으로 로그인하면 member_sno 가 바뀐다), 로그아웃 시 DELETE 로 해제. 탈퇴 시 서버가 일괄 삭제.
-- FCM 이 UNREGISTERED/INVALID_ARGUMENT 로 거절한 토큰은 발송 중 자동 삭제.

CREATE TABLE member_device_token (
    sno          BIGINT       NOT NULL AUTO_INCREMENT,
    member_sno   BIGINT       NOT NULL,
    token        VARCHAR(255) NOT NULL,
    platform     VARCHAR(10)  NOT NULL,            -- ANDROID / IOS
    last_seen_at DATETIME     NOT NULL,
    created_at   DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_member_device_token (token),
    KEY idx_member_device_token_member (member_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

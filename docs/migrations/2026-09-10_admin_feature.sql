-- 관리자 페이지(web/admin/**) 계정 테이블. member와 완전히 별개의 계정 체계이며, 회원가입 API가 없다 —
-- 최초 계정은 이 마이그레이션 실행 후 운영자가 직접 INSERT(비밀번호는 BCrypt 해시)해야 한다.
-- 세션 자체는 이 테이블이 아니라 Redis(admin:session:<token>, TTL 12h)로 관리된다.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행. 로컬(ddl-auto: update)은 자동.

CREATE TABLE admin (
    sno           BIGINT       NOT NULL AUTO_INCREMENT,
    admin_id      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_admin_id (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 회원 동의 이력(이용약관/개인정보처리방침/위치정보/알림) — 위치정보법 제16조가 요구하는
-- "언제, 어떤 버전에 동의했는지" 확인자료 보관 목적으로 버전+시각을 남긴다.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행. 로컬(ddl-auto: update)은 자동.

CREATE TABLE member_agreement (
    sno               BIGINT      NOT NULL AUTO_INCREMENT,
    member_sno        BIGINT      NOT NULL,
    agreement_type    VARCHAR(30) NOT NULL,  -- TERMS / PRIVACY / LOCATION / PUSH_SERVICE / MARKETING / MARKETING_NIGHT
    agreed            TINYINT(1)  NOT NULL,
    agreement_version VARCHAR(20) NOT NULL,
    agreed_at         DATETIME    NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uq_member_agreement (member_sno, agreement_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

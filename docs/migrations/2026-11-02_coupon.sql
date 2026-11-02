-- 관리자 쿠폰 지급(/admin/member-detail) + 앱 쿠폰함(GET /api/coupon/me).
-- 기프티콘 이미지는 S3 paceleague-media 버킷의 비공개 coupon/ 경로에 있고, image_key 가 그 객체 키.
-- expires_at 은 이 날짜(KST)까지 사용 가능. status: ISSUED / USED(회원이 직접 사용 완료 표시).
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL(paceleague-db)에 직접 실행해야 합니다.
-- 로컬(ddl-auto: update)은 자동 생성되므로 실행 불필요.

CREATE TABLE coupon (
    sno                  BIGINT       NOT NULL AUTO_INCREMENT,
    member_sno           BIGINT       NOT NULL,
    name                 VARCHAR(100) NOT NULL,
    image_key            VARCHAR(200) NOT NULL,
    expires_at           DATE         NOT NULL,
    status               VARCHAR(10)  NOT NULL,
    used_at              DATETIME     NULL,
    granted_by_admin_sno BIGINT       NOT NULL,
    created_at           DATETIME     NOT NULL,
    PRIMARY KEY (sno),
    UNIQUE KEY uk_coupon_image_key (image_key),
    KEY idx_coupon_member (member_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 게시글 이미지/동영상/링크 첨부 기능을 위한 수동 마이그레이션.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL에 직접 실행해야 합니다.
-- 로컬 개발 환경은 ddl-auto: update가 테이블을 자동 생성하므로 이 파일을 실행할 필요가 없습니다.

CREATE TABLE media (
    sno                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_sno           BIGINT       NOT NULL,
    post_sno             BIGINT       NULL,
    type                 VARCHAR(10)  NOT NULL,
    status               VARCHAR(20)  NOT NULL,
    s3_key               VARCHAR(500) NULL,
    url                  VARCHAR(1000) NULL,
    mime_type            VARCHAR(100) NULL,
    file_size_bytes      BIGINT       NULL,
    rekognition_job_id   VARCHAR(200) NULL,
    moderation_reason    VARCHAR(500) NULL,
    create_at            DATETIME     NULL,
    update_at            DATETIME     NULL,
    KEY idx_media_member_sno (member_sno),
    KEY idx_media_post_sno (post_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

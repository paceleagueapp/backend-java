-- 커뮤니티(게시판) 기능을 위한 수동 마이그레이션.
-- 이 저장소에는 마이그레이션 도구가 없습니다 (운영은 ddl-auto: validate).
-- com.example.paceleague.board 도메인을 포함한 코드를 배포하기 전에,
-- 이 SQL을 운영 MySQL에 직접(수동으로) 실행해야 합니다.
-- 로컬 개발 환경은 ddl-auto: update가 테이블을 자동 생성하므로 이 파일을 실행할 필요가 없지만,
-- 시드 데이터(보드 목록)는 로컬에서도 아래 INSERT를 직접 실행해야 합니다.

CREATE TABLE board (
    sno            BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug           VARCHAR(50)  NOT NULL,
    name           VARCHAR(50)  NOT NULL,
    description    VARCHAR(255) NULL,
    display_order  INT          NOT NULL DEFAULT 0,
    create_at      DATETIME     NULL,
    update_at      DATETIME     NULL,
    UNIQUE KEY uq_board_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post (
    sno         BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_sno   BIGINT       NOT NULL,
    member_sno  BIGINT       NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    view_count  INT          NOT NULL DEFAULT 0,
    score       INT          NOT NULL DEFAULT 0,
    create_at   DATETIME     NULL,
    update_at   DATETIME     NULL,
    KEY idx_post_board_sno (board_sno),
    KEY idx_post_member_sno (member_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comment (
    sno                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_sno            BIGINT        NOT NULL,
    member_sno          BIGINT        NOT NULL,
    parent_comment_sno  BIGINT        NULL,
    content             VARCHAR(1000) NOT NULL,
    score               INT           NOT NULL DEFAULT 0,
    create_at           DATETIME      NULL,
    update_at           DATETIME      NULL,
    KEY idx_comment_post_sno (post_sno),
    KEY idx_comment_parent_comment_sno (parent_comment_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- vote_value는 값 범위만 보면 TINYINT로 충분하지만 INT로 둔다: PostVote/CommentVote 엔티티의
-- Java 필드가 int라서, Hibernate가 ddl-auto: validate 시 INTEGER 타입을 기대함(TINYINT로 만들면
-- 배포 시 "wrong column type" 에러로 앱이 기동 실패한다 — 2026-08-08 실제로 발생했던 문제).
CREATE TABLE post_vote (
    sno         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_sno    BIGINT   NOT NULL,
    member_sno  BIGINT   NOT NULL,
    vote_value  INT      NOT NULL,
    create_at   DATETIME NULL,
    update_at   DATETIME NULL,
    UNIQUE KEY uq_post_vote_member_post (member_sno, post_sno),
    KEY idx_post_vote_post_sno (post_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comment_vote (
    sno          BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_sno  BIGINT   NOT NULL,
    member_sno   BIGINT   NOT NULL,
    vote_value   TINYINT  NOT NULL,
    create_at    DATETIME NULL,
    update_at    DATETIME NULL,
    UNIQUE KEY uq_comment_vote_member_comment (member_sno, comment_sno),
    KEY idx_comment_vote_comment_sno (comment_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 초기 보드 시드
INSERT INTO board (slug, name, description, display_order, create_at, update_at) VALUES
  ('free',   '자유게시판', '자유롭게 이야기하는 공간입니다.', 1, NOW(), NOW()),
  ('qna',    '질문',       '러닝 관련 질문을 올려주세요.',   2, NOW(), NOW()),
  ('verify', '인증',       '오늘의 러닝을 인증해보세요.',     3, NOW(), NOW());

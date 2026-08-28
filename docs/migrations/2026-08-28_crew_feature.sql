-- 크루(Crew) 1단계 기능을 위한 수동 마이그레이션.
-- 운영은 ddl-auto: validate라 배포 전 이 SQL을 운영 MySQL/MariaDB에 직접 실행해야 합니다.
-- 로컬 개발 환경은 ddl-auto: update가 자동 생성하므로 실행할 필요가 없습니다.
--
-- member 참조 컬럼은 record_track.uno / member_score.member_sno 와 동일하게 BIGINT.
-- "한 회원 = 한 크루" 는 crew_member.member_sno 전역 UNIQUE로 강제한다.

CREATE TABLE crew (
    sno               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(30)   NOT NULL,
    icon_url          VARCHAR(1000) NULL,
    description       VARCHAR(500)  NULL,
    notice            VARCHAR(2000) NULL,
    join_policy       VARCHAR(20)   NULL,        -- APPROVAL (v1은 이 값만)
    member_limit      INT           NOT NULL,
    leader_member_sno BIGINT        NOT NULL,
    member_count      INT           NOT NULL,
    status            VARCHAR(20)   NULL,        -- ACTIVE (해체는 하드 삭제)
    create_at         DATETIME      NULL,
    update_at         DATETIME      NULL,
    UNIQUE KEY uk_crew_name (name),
    KEY idx_crew_leader (leader_member_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE crew_member (
    sno        BIGINT AUTO_INCREMENT PRIMARY KEY,
    crew_sno   BIGINT      NOT NULL,
    member_sno BIGINT      NOT NULL,
    role       VARCHAR(20) NULL,                 -- LEADER / MEMBER
    joined_at  DATETIME    NULL,
    UNIQUE KEY uk_crew_member_pair (crew_sno, member_sno),
    UNIQUE KEY uk_crew_member_member (member_sno),  -- 한 회원 = 한 크루
    KEY idx_crew_member_crew (crew_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE crew_invitation (
    sno                BIGINT AUTO_INCREMENT PRIMARY KEY,
    crew_sno           BIGINT      NOT NULL,
    inviter_member_sno BIGINT      NOT NULL,
    invitee_member_sno BIGINT      NOT NULL,
    status             VARCHAR(20) NULL,          -- PENDING/ACCEPTED/DECLINED/CANCELED/EXPIRED
    create_at          DATETIME    NULL,
    expires_at         DATETIME    NULL,
    KEY idx_crew_invitation_invitee (invitee_member_sno, status),
    KEY idx_crew_invitation_crew (crew_sno, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE crew_join_request (
    sno        BIGINT AUTO_INCREMENT PRIMARY KEY,
    crew_sno   BIGINT       NOT NULL,
    member_sno BIGINT       NOT NULL,
    status     VARCHAR(20)  NULL,                 -- PENDING/APPROVED/REJECTED/CANCELED
    message    VARCHAR(300) NULL,
    create_at  DATETIME     NULL,
    decided_at DATETIME     NULL,
    KEY idx_crew_join_request_crew (crew_sno, status),
    KEY idx_crew_join_request_member (member_sno, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

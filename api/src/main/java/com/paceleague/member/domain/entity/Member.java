package com.paceleague.member.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

        public static final String STATUS_ACTIVE = "ACTIVE";
        public static final String STATUS_WITHDRAWN = "WITHDRAWN";

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer sno;

        @Column(name = "member_id", nullable = false, unique = true, length = 50)
        private String memberId;

        @Column(name = "password_hash", nullable = false, length = 255)
        private String passwordHash;

        @Column(length = 50)
        private String nickname;

        @Column(length = 50)
        private String email;

        @Column(name = "status", nullable = false, length = 20)
        private String status = STATUS_ACTIVE;

        @Column(name = "withdrawn_at")
        private LocalDateTime withdrawnAt;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        public Member(String memberId, String passwordHash, String nickname, String email) {
                this.memberId = memberId;
                this.passwordHash = passwordHash;
                this.nickname = nickname;
                this.email = email;
                this.status = STATUS_ACTIVE;
                this.createdAt = Instant.now();
                this.updatedAt = this.createdAt;
        }

        public static Member create(String memberId, String passwordHash, String nickname, String email) {
                return new Member(memberId, passwordHash, nickname, email);
        }

        public boolean isActive() {
                return STATUS_ACTIVE.equals(this.status);
        }

        // 회원 탈퇴 — 행은 남기되 개인정보를 즉시 제거하고, 아이디는 재사용 가능하도록 마커로 치환한다.
        // (글/댓글의 member_sno 는 그대로 두어 "탈퇴한 사용자"로 익명 표시된다 — GetMemberNicknamePort 참조.)
        public void withdraw() {
                this.status = STATUS_WITHDRAWN;
                this.withdrawnAt = LocalDateTime.now();
                this.memberId = "withdrawn_" + this.sno;
                this.nickname = null;
                this.email = null;
                this.passwordHash = ""; // matches() 가 절대 통과 못 함
        }

        @PreUpdate
        public void preUpdate() {
                this.updatedAt = Instant.now();
        }
}

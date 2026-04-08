package com.example.paceleague.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

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

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        public Member(String memberId, String passwordHash, String nickname, String email) {
                this.memberId = memberId;
                this.passwordHash = passwordHash;
                this.nickname = nickname;
                this.email = email;
                this.createdAt = Instant.now();
                this.updatedAt = this.createdAt;
        }

        public static Member create(String memberId, String passwordHash, String nickname, String email) {
                return new Member(memberId, passwordHash, nickname, email);
        }

        @PreUpdate
        public void preUpdate() {
                this.updatedAt = Instant.now();
        }
}

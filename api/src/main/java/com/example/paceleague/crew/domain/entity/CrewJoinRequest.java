package com.example.paceleague.crew.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 회원 → 크루 가입신청. 크루장이 승인하면 가입.
@Entity
@Table(name = "crew_join_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrewJoinRequest {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELED = "CANCELED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "crew_sno", nullable = false)
    private Long crewSno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "message", length = 300)
    private String message;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    private CrewJoinRequest(Long crewSno, Long memberSno, String message) {
        this.crewSno = crewSno;
        this.memberSno = memberSno;
        this.message = message;
        this.status = STATUS_PENDING;
        this.createAt = LocalDateTime.now();
    }

    public static CrewJoinRequest create(Long crewSno, Long memberSno, String message) {
        return new CrewJoinRequest(crewSno, memberSno, message);
    }

    public void approve() {
        this.status = STATUS_APPROVED;
        this.decidedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = STATUS_REJECTED;
        this.decidedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = STATUS_CANCELED;
        this.decidedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(this.status);
    }
}

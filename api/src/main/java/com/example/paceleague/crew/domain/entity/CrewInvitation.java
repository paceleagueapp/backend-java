package com.example.paceleague.crew.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 크루장 → 특정 회원 초대. 회원이 수락하면 가입. 일정 기간 후 만료.
@Entity
@Table(name = "crew_invitation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrewInvitation {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_CANCELED = "CANCELED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "crew_sno", nullable = false)
    private Long crewSno;

    @Column(name = "inviter_member_sno", nullable = false)
    private Long inviterMemberSno;

    @Column(name = "invitee_member_sno", nullable = false)
    private Long inviteeMemberSno;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private CrewInvitation(Long crewSno, Long inviterMemberSno, Long inviteeMemberSno, LocalDateTime expiresAt) {
        this.crewSno = crewSno;
        this.inviterMemberSno = inviterMemberSno;
        this.inviteeMemberSno = inviteeMemberSno;
        this.status = STATUS_PENDING;
        this.createAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public static CrewInvitation create(Long crewSno, Long inviterMemberSno, Long inviteeMemberSno,
                                        LocalDateTime expiresAt) {
        return new CrewInvitation(crewSno, inviterMemberSno, inviteeMemberSno, expiresAt);
    }

    public void accept() {
        this.status = STATUS_ACCEPTED;
    }

    public void decline() {
        this.status = STATUS_DECLINED;
    }

    public void cancel() {
        this.status = STATUS_CANCELED;
    }

    public void expire() {
        this.status = STATUS_EXPIRED;
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(this.status);
    }

    public boolean isExpired(LocalDateTime now) {
        return this.expiresAt != null && now.isAfter(this.expiresAt);
    }
}

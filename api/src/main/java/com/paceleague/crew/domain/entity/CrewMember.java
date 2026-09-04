package com.paceleague.crew.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 크루-회원 소속 관계. (crew_sno, member_sno) UNIQUE + member_sno 전역 UNIQUE(한 회원 한 크루).
@Entity
@Table(name = "crew_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrewMember {

    public static final String ROLE_LEADER = "LEADER";
    public static final String ROLE_MEMBER = "MEMBER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "crew_sno", nullable = false)
    private Long crewSno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    private CrewMember(Long crewSno, Long memberSno, String role) {
        this.crewSno = crewSno;
        this.memberSno = memberSno;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public static CrewMember leader(Long crewSno, Long memberSno) {
        return new CrewMember(crewSno, memberSno, ROLE_LEADER);
    }

    public static CrewMember member(Long crewSno, Long memberSno) {
        return new CrewMember(crewSno, memberSno, ROLE_MEMBER);
    }

    public void promoteToLeader() {
        this.role = ROLE_LEADER;
    }

    public void demoteToMember() {
        this.role = ROLE_MEMBER;
    }

    public boolean isLeader() {
        return ROLE_LEADER.equals(this.role);
    }
}

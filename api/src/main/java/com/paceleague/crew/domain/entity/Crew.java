package com.paceleague.crew.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 크루 = 회원의 모임(게임의 길드). 크루장이 만들고 크루원을 모집한다.
//  - 한 회원 = 한 크루 (crew_member.member_sno 전역 UNIQUE로 강제)
//  - join_policy 는 v1에서 APPROVAL(승인제)만 사용. 컬럼은 후속 확장 대비로 둔다.
@Entity
@Table(name = "crew")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crew {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DISBANDED = "DISBANDED";
    public static final String JOIN_POLICY_APPROVAL = "APPROVAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    @Column(name = "icon_url", length = 1000)
    private String iconUrl;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "notice", length = 2000)
    private String notice;

    @Column(name = "join_policy", length = 20)
    private String joinPolicy;

    @Column(name = "member_limit", nullable = false)
    private int memberLimit;

    @Column(name = "leader_member_sno", nullable = false)
    private Long leaderMemberSno;

    @Column(name = "member_count", nullable = false)
    private int memberCount;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    private Crew(String name, String iconUrl, String description, String joinPolicy,
                 int memberLimit, Long leaderMemberSno) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.description = description;
        this.joinPolicy = joinPolicy;
        this.memberLimit = memberLimit;
        this.leaderMemberSno = leaderMemberSno;
        this.memberCount = 1; // 생성자가 첫 크루원(크루장)
        this.status = STATUS_ACTIVE;
        this.createAt = LocalDateTime.now();
        this.updateAt = this.createAt;
    }

    public static Crew create(String name, String iconUrl, String description,
                              int memberLimit, Long leaderMemberSno) {
        return new Crew(name, iconUrl, description, JOIN_POLICY_APPROVAL, memberLimit, leaderMemberSno);
    }

    public void updateInfo(String name, String iconUrl, String description) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.description = description;
        this.updateAt = LocalDateTime.now();
    }

    public void updateNotice(String notice) {
        this.notice = notice;
        this.updateAt = LocalDateTime.now();
    }

    public void changeLeader(Long newLeaderMemberSno) {
        this.leaderMemberSno = newLeaderMemberSno;
        this.updateAt = LocalDateTime.now();
    }

    public void increaseMemberCount() {
        this.memberCount += 1;
        this.updateAt = LocalDateTime.now();
    }

    public void decreaseMemberCount() {
        this.memberCount = Math.max(0, this.memberCount - 1);
        this.updateAt = LocalDateTime.now();
    }

    public void disband() {
        this.status = STATUS_DISBANDED;
        this.updateAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(this.status);
    }

    public boolean isFull() {
        return this.memberCount >= this.memberLimit;
    }

    public boolean isLeader(Long memberSno) {
        return memberSno != null && memberSno.equals(this.leaderMemberSno);
    }
}

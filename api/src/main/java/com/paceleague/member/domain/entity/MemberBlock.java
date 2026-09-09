package com.paceleague.member.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 회원 차단(단방향). blocker 가 blocked 의 글/댓글을 게시판 피드에서 안 보게 된다.
@Entity
@Table(name = "member_block")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "blocker_member_sno", nullable = false)
    private Long blockerMemberSno;

    @Column(name = "blocked_member_sno", nullable = false)
    private Long blockedMemberSno;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private MemberBlock(Long blockerMemberSno, Long blockedMemberSno) {
        this.blockerMemberSno = blockerMemberSno;
        this.blockedMemberSno = blockedMemberSno;
        this.createdAt = LocalDateTime.now();
    }

    public static MemberBlock create(Long blockerMemberSno, Long blockedMemberSno) {
        return new MemberBlock(blockerMemberSno, blockedMemberSno);
    }
}

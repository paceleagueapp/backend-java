package com.example.paceleague.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "post_sno", nullable = false)
    private Long postSno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "parent_comment_sno")
    private Long parentCommentSno;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private int score;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    public Comment(Long postSno, Long memberSno, Long parentCommentSno, String content) {
        this.postSno = postSno;
        this.memberSno = memberSno;
        this.parentCommentSno = parentCommentSno;
        this.content = content;
        this.score = 0;
        this.createAt = LocalDateTime.now(ZoneOffset.UTC);
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public static Comment create(Long postSno, Long memberSno, Long parentCommentSno, String content) {
        return new Comment(postSno, memberSno, parentCommentSno, content);
    }

    public boolean isReply() {
        return parentCommentSno != null;
    }

    public void applyVoteDelta(int delta) {
        this.score += delta;
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}

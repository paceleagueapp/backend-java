package com.example.paceleague.board.domain.entity;

import com.example.paceleague.board.domain.enums.VoteType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "comment_vote", uniqueConstraints = @UniqueConstraint(columnNames = {"member_sno", "comment_sno"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "comment_sno", nullable = false)
    private Long commentSno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "vote_value", nullable = false)
    private int voteValue;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    public CommentVote(Long commentSno, Long memberSno, VoteType voteType) {
        this.commentSno = commentSno;
        this.memberSno = memberSno;
        this.voteValue = voteType.getValue();
        this.createAt = LocalDateTime.now(ZoneOffset.UTC);
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public static CommentVote create(Long commentSno, Long memberSno, VoteType voteType) {
        return new CommentVote(commentSno, memberSno, voteType);
    }

    public void changeValue(VoteType voteType) {
        this.voteValue = voteType.getValue();
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}

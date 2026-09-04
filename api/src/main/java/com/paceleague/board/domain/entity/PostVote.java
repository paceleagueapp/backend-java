package com.paceleague.board.domain.entity;

import com.paceleague.board.domain.enums.VoteType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "post_vote", uniqueConstraints = @UniqueConstraint(columnNames = {"member_sno", "post_sno"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "post_sno", nullable = false)
    private Long postSno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "vote_value", nullable = false)
    private int voteValue;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    public PostVote(Long postSno, Long memberSno, VoteType voteType) {
        this.postSno = postSno;
        this.memberSno = memberSno;
        this.voteValue = voteType.getValue();
        this.createAt = LocalDateTime.now(ZoneOffset.UTC);
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public static PostVote create(Long postSno, Long memberSno, VoteType voteType) {
        return new PostVote(postSno, memberSno, voteType);
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

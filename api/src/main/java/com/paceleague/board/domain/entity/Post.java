package com.paceleague.board.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "board_sno", nullable = false)
    private Long boardSno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "record_sno")
    private Long recordSno;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int score;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    public Post(Long boardSno, Long memberSno, Long recordSno, String title, String content) {
        this.boardSno = boardSno;
        this.memberSno = memberSno;
        this.recordSno = recordSno;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.score = 0;
        this.createAt = LocalDateTime.now(ZoneOffset.UTC);
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public static Post create(Long boardSno, Long memberSno, Long recordSno, String title, String content) {
        return new Post(boardSno, memberSno, recordSno, title, content);
    }

    public void applyVoteDelta(int delta) {
        this.score += delta;
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void edit(String title, String content, Long recordSno) {
        this.title = title;
        this.content = content;
        this.recordSno = recordSno;
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}

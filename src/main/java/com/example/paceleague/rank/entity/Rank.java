package com.example.paceleague.rank.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_rank")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "uno")
    private Long uno;

    @Column(name = "score")
    private Integer score;

    @Column(name = "scaled_score")
    private Integer scaledScore;

    @Column(name = "add_score")
    private Integer addScore;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "utc_offset")
    private String utcOffset;

    public Rank(Long uno, Integer score, Integer scaledScore, Integer addScore, String utcOffset) {
        this.uno = uno;
        this.score = score;
        this.scaledScore = scaledScore;
        this.addScore = addScore;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
        this.utcOffset = utcOffset;
    }

    public static Rank create(Long uno, Integer score, Integer scaledScore, Integer addScore, String utcOffset){
        return new Rank(uno, score, scaledScore, addScore, utcOffset);
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}

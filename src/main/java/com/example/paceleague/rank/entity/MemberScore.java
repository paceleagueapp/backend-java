package com.example.paceleague.rank.entity;

import com.example.paceleague.rank.enums.RankTier;
import com.example.paceleague.rank.policy.RankTierPolicy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "season_sno", nullable = false)
    private Long seasonSno;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 30)
    private RankTier tier;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "utc_offset", length = 50)
    private String utcOffset;

    @Builder
    public MemberScore(Long memberSno, Long seasonSno, int totalScore, String utcOffset) {
        this.memberSno = memberSno;
        this.seasonSno = seasonSno;
        this.totalScore = totalScore;
        this.tier = RankTierPolicy.calculate(totalScore);
        this.utcOffset = utcOffset;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    public void addScore(int score) {
        this.totalScore += score;
        this.tier = RankTierPolicy.calculate(this.totalScore);
        this.updateAt = LocalDateTime.now();
    }
}

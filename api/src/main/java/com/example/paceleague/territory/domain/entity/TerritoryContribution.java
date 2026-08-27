package com.example.paceleague.territory.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 한 땅에 대한 공격 기여도 한 건(러닝 1회 = 최대 1건). HP 0 시점에 1시간 윈도우로 합산해 점령자를 정한다.
// 점령이 확정되면 해당 territory의 기여도 행은 모두 삭제되어 새로 시작한다.
@Entity
@Table(name = "territory_contribution")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TerritoryContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "territory_sno", nullable = false)
    private Long territorySno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "damage", nullable = false)
    private int damage;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    private TerritoryContribution(Long territorySno, Long memberSno, int damage) {
        this.territorySno = territorySno;
        this.memberSno = memberSno;
        this.damage = damage;
        this.createAt = LocalDateTime.now();
    }

    public static TerritoryContribution of(Long territorySno, Long memberSno, int damage) {
        return new TerritoryContribution(territorySno, memberSno, damage);
    }
}

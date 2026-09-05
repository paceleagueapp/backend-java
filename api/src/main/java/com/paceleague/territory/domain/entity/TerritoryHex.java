package com.paceleague.territory.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// H3(resolution 12) 헥사곤 1개 = 어느 territory에 속하는지 매핑.
// h3_index를 PK로 둬서 "한 헥사곤은 동시에 하나의 ACTIVE territory에만 속한다"는 불변식을
// DB 유니크 제약으로도 보장한다. 소유자/HP는 territory 쪽에만 있고(단위 유지),
// 이 행은 territory_sno가 정해진 뒤로 캡처가 일어나도 절대 바뀌지 않는다 — 점령은 territory.owner_member_sno만 바꾼다.
@Entity
@Table(name = "territory_hex")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TerritoryHex {

    @Id
    @Column(name = "h3_index")
    private Long h3Index;

    @Column(name = "territory_sno", nullable = false)
    private Long territorySno;

    @Column(name = "season")
    private Long season;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    private TerritoryHex(Long h3Index, Long territorySno, Long season) {
        this.h3Index = h3Index;
        this.territorySno = territorySno;
        this.season = season;
        this.createAt = LocalDateTime.now();
    }

    public static TerritoryHex of(Long h3Index, Long territorySno, Long season) {
        return new TerritoryHex(h3Index, territorySno, season);
    }
}

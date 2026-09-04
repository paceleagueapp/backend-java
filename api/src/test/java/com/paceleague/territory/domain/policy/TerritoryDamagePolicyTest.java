package com.paceleague.territory.domain.policy;

import com.paceleague.territory.domain.policy.TerritoryDamagePolicy.Contribution;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TerritoryDamagePolicyTest {

    @Test
    void 데미지는_겹친_면적_비율에_비례한다() {
        // 완전히 겹침(비율 1) → maxHp 100 × attackFactor 0.5 = 50
        assertThat(TerritoryDamagePolicy.damage(10_000, 10_000, 100, 0.5)).isEqualTo(50);
        // 절반 겹침 → 25
        assertThat(TerritoryDamagePolicy.damage(5_000, 10_000, 100, 0.5)).isEqualTo(25);
    }

    @Test
    void 겹침이_없으면_데미지_0이다() {
        assertThat(TerritoryDamagePolicy.damage(0, 10_000, 100, 0.5)).isZero();
    }

    @Test
    void 겹침이_아주_작아도_최소_1의_데미지는_들어간다() {
        assertThat(TerritoryDamagePolicy.damage(1, 10_000, 100, 0.5)).isEqualTo(1);
    }

    @Test
    void 회복량도_겹친_면적_비율에_비례한다() {
        assertThat(TerritoryDamagePolicy.heal(10_000, 10_000, 100, 0.5)).isEqualTo(50);
        assertThat(TerritoryDamagePolicy.heal(2_500, 10_000, 100, 0.5)).isEqualTo(13); // round(0.25*100*0.5)
    }

    @Test
    void 점령자는_1시간_윈도우_기여도_합이_가장_큰_사람이다() {
        LocalDateTime now = LocalDateTime.now();
        List<Contribution> prior = List.of(
                new Contribution(1L, 50, now.minusMinutes(30)),
                new Contribution(2L, 20, now.minusMinutes(10))
        );
        // 이번 공격자 2번이 30 더 넣어도 총합은 1번(50) > 2번(50)? -> 2번 20+30=50, 1번 50 동점
        Long owner = TerritoryDamagePolicy.resolveNewOwner(prior, 2L, 30, now);
        // 동점이면 가장 최근 기여자 → 2번(now)
        assertThat(owner).isEqualTo(2L);
    }

    @Test
    void 기여도_합이_확실히_큰_사람이_점령한다() {
        LocalDateTime now = LocalDateTime.now();
        List<Contribution> prior = List.of(
                new Contribution(1L, 80, now.minusMinutes(40)),
                new Contribution(2L, 10, now.minusMinutes(5))
        );
        Long owner = TerritoryDamagePolicy.resolveNewOwner(prior, 2L, 15, now);
        assertThat(owner).isEqualTo(1L); // 1번 80 vs 2번 25
    }

    @Test
    void 사전_기여도가_없으면_이번_공격자가_점령한다() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(TerritoryDamagePolicy.resolveNewOwner(List.of(), 7L, 100, now)).isEqualTo(7L);
    }
}

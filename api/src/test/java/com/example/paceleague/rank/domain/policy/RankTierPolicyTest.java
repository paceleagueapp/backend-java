package com.example.paceleague.rank.domain.policy;

import com.example.paceleague.rank.domain.enums.RankTier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RankTierPolicyTest {

    @Test
    void 점수가_음수여도_최소_브론즈다() {
        assertThat(RankTierPolicy.calculate(-100)).isEqualTo(RankTier.BRONZE);
    }

    @Test
    void 각_티어의_최소_점수_경계에서_해당_티어로_승급한다() {
        assertThat(RankTierPolicy.calculate(1499)).isEqualTo(RankTier.BRONZE);
        assertThat(RankTierPolicy.calculate(1500)).isEqualTo(RankTier.SILVER);
        assertThat(RankTierPolicy.calculate(2999)).isEqualTo(RankTier.SILVER);
        assertThat(RankTierPolicy.calculate(3000)).isEqualTo(RankTier.GOLD);
    }

    @Test
    void 최고_점수는_챌린저를_반환한다() {
        assertThat(RankTierPolicy.calculate(999_999)).isEqualTo(RankTier.CHALLENGER);
    }
}

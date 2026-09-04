package com.paceleague.rank.domain.policy;

import com.paceleague.common.i18n.Language;
import com.paceleague.rank.domain.enums.RankTier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RankTierLabelPolicyTest {

    @Test
    void 한국어_라벨을_반환한다() {
        assertThat(RankTierLabelPolicy.label(RankTier.GOLD, Language.KO)).isEqualTo("골드");
    }

    @Test
    void 영어_라벨을_반환한다() {
        assertThat(RankTierLabelPolicy.label(RankTier.CHALLENGER, Language.EN)).isEqualTo("Challenger");
    }

    @Test
    void 모든_티어와_언어_조합에_라벨이_존재한다() {
        for (RankTier tier : RankTier.values()) {
            for (Language lang : Language.values()) {
                assertThat(RankTierLabelPolicy.label(tier, lang))
                        .as("tier=%s lang=%s", tier, lang)
                        .isNotBlank();
            }
        }
    }
}

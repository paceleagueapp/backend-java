package com.example.paceleague.record.domain.policy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RecordScoreCalculatorTest {

    @Test
    void 기본_점수는_거리_km_에_10을_곱해_반올림한다() {
        assertThat(RecordScoreCalculator.calculateBaseScore(BigDecimal.valueOf(5.0))).isEqualTo(50);
        assertThat(RecordScoreCalculator.calculateBaseScore(BigDecimal.valueOf(5.04))).isEqualTo(50);
        assertThat(RecordScoreCalculator.calculateBaseScore(BigDecimal.valueOf(5.05))).isEqualTo(51);
    }

    @Test
    void 페이스가_5분30초_이내면_보너스_20퍼센트를_받는다() {
        int bonus = RecordScoreCalculator.calculatePaceBonus(50, 1650, BigDecimal.valueOf(5));
        assertThat(bonus).isEqualTo(10);
    }

    @Test
    void 페이스가_5분30초_초과_6분30초_이내면_보너스_10퍼센트를_받는다() {
        int bonus = RecordScoreCalculator.calculatePaceBonus(50, 1755, BigDecimal.valueOf(5));
        assertThat(bonus).isEqualTo(5);
    }

    @Test
    void 페이스가_6분30초_경계값이면_보너스_10퍼센트를_받는다() {
        int bonus = RecordScoreCalculator.calculatePaceBonus(50, 1950, BigDecimal.valueOf(5));
        assertThat(bonus).isEqualTo(5);
    }

    @Test
    void 페이스가_6분30초를_초과하면_보너스가_없다() {
        int bonus = RecordScoreCalculator.calculatePaceBonus(50, 2000, BigDecimal.valueOf(5));
        assertThat(bonus).isZero();
    }
}

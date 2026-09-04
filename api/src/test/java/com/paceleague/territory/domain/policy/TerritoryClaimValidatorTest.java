package com.paceleague.territory.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TerritoryClaimValidatorTest {

    private static final double MIN_PERIMETER = 300.0;
    private static final double MIN_AREA = 10_000.0;
    private static final double MAX_AREA = 5_000_000.0;

    private static boolean check(double perimeter, double area) {
        return TerritoryClaimValidator.isClaimable(perimeter, area, MIN_PERIMETER, MIN_AREA, MAX_AREA);
    }

    @Test
    void 둘레와_면적이_범위_안이면_인정된다() {
        assertThat(check(400.0, 50_000.0)).isTrue();
    }

    @Test
    void 둘레가_최소_미만이면_거부된다() {
        assertThat(check(299.9, 50_000.0)).isFalse();
    }

    @Test
    void 면적이_최소_미만이면_거부된다() {
        assertThat(check(400.0, 9_999.9)).isFalse();
    }

    @Test
    void 면적이_최대_초과면_거부된다() {
        assertThat(check(400.0, 5_000_000.1)).isFalse();
    }

    @Test
    void 경계값은_모두_인정된다() {
        assertThat(check(300.0, 10_000.0)).isTrue();
        assertThat(check(300.0, 5_000_000.0)).isTrue();
    }
}

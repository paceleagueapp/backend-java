package com.example.paceleague.crew.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrewNamePolicyTest {

    @Test
    void 앞뒤_공백을_다듬어_돌려준다() {
        assertThat(CrewNamePolicy.normalizeAndValidate("  달리는곰  ", 2, 20)).isEqualTo("달리는곰");
    }

    @Test
    void 비어있으면_거부한다() {
        assertThatThrownBy(() -> CrewNamePolicy.normalizeAndValidate("   ", 2, 20))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CrewNamePolicy.normalizeAndValidate(null, 2, 20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 최소_길이_미만이면_거부한다() {
        assertThatThrownBy(() -> CrewNamePolicy.normalizeAndValidate("가", 2, 20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 최대_길이_초과면_거부한다() {
        assertThatThrownBy(() -> CrewNamePolicy.normalizeAndValidate("가".repeat(21), 2, 20))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 경계값은_통과한다() {
        assertThat(CrewNamePolicy.normalizeAndValidate("가나", 2, 20)).isEqualTo("가나");
        assertThat(CrewNamePolicy.normalizeAndValidate("가".repeat(20), 2, 20)).hasSize(20);
    }
}

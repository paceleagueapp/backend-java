package com.example.paceleague.record.domain.policy;

import com.example.paceleague.record.application.dto.RecordSummaryDto;
import com.example.paceleague.record.application.dto.RecordSummaryProjection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecordSummaryCalculatorTest {

    @Test
    void 거리와_시간이_있으면_페이스와_칼로리를_계산한다() {
        RecordSummaryProjection projection = mock(RecordSummaryProjection.class);
        when(projection.getTotalDistance()).thenReturn(BigDecimal.valueOf(5000));
        when(projection.getTotalDurationSeconds()).thenReturn(1500L);

        RecordSummaryDto result = RecordSummaryCalculator.from(projection, BigDecimal.valueOf(70));

        assertThat(result.totalDistance()).isEqualByComparingTo("5000");
        assertThat(result.totalDurationSeconds()).isEqualTo(1500L);
        assertThat(result.paceSecondsPerKm()).isEqualTo(300L);
        assertThat(result.paceText()).isEqualTo("5:00 /km");
        assertThat(result.totalCalories()).isEqualByComparingTo("350.0");
    }

    @Test
    void 거리가_없으면_페이스는_0이고_칼로리도_0이다() {
        RecordSummaryProjection projection = mock(RecordSummaryProjection.class);
        when(projection.getTotalDistance()).thenReturn(null);
        when(projection.getTotalDurationSeconds()).thenReturn(null);

        RecordSummaryDto result = RecordSummaryCalculator.from(projection, BigDecimal.valueOf(70));

        assertThat(result.totalDistance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalDurationSeconds()).isZero();
        assertThat(result.paceSecondsPerKm()).isZero();
        assertThat(result.paceText()).isEqualTo("0:00 /km");
        assertThat(result.totalCalories()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void 몸무게가_없으면_거리가_있어도_칼로리는_0이다() {
        RecordSummaryProjection projection = mock(RecordSummaryProjection.class);
        when(projection.getTotalDistance()).thenReturn(BigDecimal.valueOf(5000));
        when(projection.getTotalDurationSeconds()).thenReturn(1500L);

        RecordSummaryDto result = RecordSummaryCalculator.from(projection, null);

        assertThat(result.totalCalories()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.paceSecondsPerKm()).isEqualTo(300L);
    }
}

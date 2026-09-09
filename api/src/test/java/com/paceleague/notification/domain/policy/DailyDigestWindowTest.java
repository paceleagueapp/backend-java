package com.paceleague.notification.domain.policy;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DailyDigestWindowTest {

    @Test
    void 저장TZ가_UTC면_KST_하루가_전날_15시부터_당일_15시_UTC로_변환된다() {
        DailyDigestWindow.Window w = DailyDigestWindow.forDate(LocalDate.of(2026, 9, 8), ZoneOffset.UTC);

        assertThat(w.fromInclusive()).isEqualTo(LocalDateTime.of(2026, 9, 7, 15, 0));
        assertThat(w.toExclusive()).isEqualTo(LocalDateTime.of(2026, 9, 8, 15, 0));
    }

    @Test
    void 저장TZ가_KST면_KST_하루가_그대로_00시부터_24시가_된다() {
        DailyDigestWindow.Window w = DailyDigestWindow.forDate(LocalDate.of(2026, 9, 8), ZoneId.of("Asia/Seoul"));

        assertThat(w.fromInclusive()).isEqualTo(LocalDateTime.of(2026, 9, 8, 0, 0));
        assertThat(w.toExclusive()).isEqualTo(LocalDateTime.of(2026, 9, 9, 0, 0));
    }

    @Test
    void 구간_길이는_항상_24시간이다() {
        DailyDigestWindow.Window w = DailyDigestWindow.forDate(LocalDate.of(2026, 1, 1), ZoneOffset.UTC);

        assertThat(java.time.Duration.between(w.fromInclusive(), w.toExclusive()).toHours()).isEqualTo(24);
    }

    @Test
    void yesterdayKst는_KST_기준_오늘보다_하루_전이다() {
        assertThat(DailyDigestWindow.yesterdayKst())
                .isEqualTo(LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1));
    }
}

package com.example.paceleague.record.domain.policy;

import com.example.paceleague.record.application.dto.GpsSessionRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GpsSessionValidatorTest {

    private static final OffsetDateTime START = OffsetDateTime.of(2026, 8, 26, 10, 46, 25, 0, ZoneOffset.UTC);
    private static final OffsetDateTime END = START.plusMinutes(32);

    private static GpsSessionRequest.GpsPoint point(double lat, double lng) {
        return new GpsSessionRequest.GpsPoint(0, START, lat, lng, 12.4, 8.1, lat, lng);
    }

    private static GpsSessionRequest valid() {
        return new GpsSessionRequest(
                1, "run-1", "RUNNING", "FINISHED",
                START, END, 1_946_745L, new BigDecimal("5154.81"), 2,
                new GpsSessionRequest.Location(3000, new BigDecimal("5"), "kalman-v2"),
                new GpsSessionRequest.Device("ANDROID", "1.0.24", 24),
                List.of(point(37.5908521, 126.704748), point(37.5908142, 126.7048013)),
                "+09:00"
        );
    }

    @Test
    void 정상_페이로드는_통과한다() {
        assertThatCode(() -> GpsSessionValidator.validate(1L, valid())).doesNotThrowAnyException();
    }

    @Test
    void uno가_없으면_거부한다() {
        assertThatThrownBy(() -> GpsSessionValidator.validate(null, valid()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void clientRunId가_비면_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest(
                1, "  ", "RUNNING", "FINISHED", START, END, 1L, BigDecimal.TEN, 1,
                null, null, List.of(point(37.0, 127.0)), null);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void FINISHED가_아닌_세션은_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest(
                1, "run-1", "RUNNING", "IN_PROGRESS", START, END, 1L, BigDecimal.TEN, 1,
                null, null, List.of(point(37.0, 127.0)), null);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void RUNNING이_아닌_활동은_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest(
                1, "run-1", "CYCLING", "FINISHED", START, END, 1L, BigDecimal.TEN, 1,
                null, null, List.of(point(37.0, 127.0)), null);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void endedAt이_startedAt보다_빠르면_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest(
                1, "run-1", "RUNNING", "FINISHED", END, START, 1L, BigDecimal.TEN, 1,
                null, null, List.of(point(37.0, 127.0)), null);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void points가_비면_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest(
                1, "run-1", "RUNNING", "FINISHED", START, END, 1L, BigDecimal.TEN, 0,
                null, null, List.of(), null);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 좌표범위를_벗어나면_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest(
                1, "run-1", "RUNNING", "FINISHED", START, END, 1L, BigDecimal.TEN, 1,
                null, null, List.of(point(91.0, 127.0)), null);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void distanceMeters가_0이하면_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest(
                1, "run-1", "RUNNING", "FINISHED", START, END, 1L, BigDecimal.ZERO, 1,
                null, null, List.of(point(37.0, 127.0)), null);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

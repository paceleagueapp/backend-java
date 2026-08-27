package com.example.paceleague.record.domain.policy;

import com.example.paceleague.record.application.dto.GpsSessionRequest;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GpsSessionValidatorTest {

    private static final OffsetDateTime T0 = OffsetDateTime.of(2026, 8, 26, 10, 46, 25, 0, ZoneOffset.UTC);

    private static GpsSessionRequest.GpsPoint point(double lat, double lng) {
        return new GpsSessionRequest.GpsPoint(0, T0, lat, lng, 12.4, 8.1, lat, lng);
    }

    private static GpsSessionRequest chunk(List<GpsSessionRequest.GpsPoint> points, Boolean finished) {
        return new GpsSessionRequest("run-1", "RUNNING", points, finished, null, null, 1, "+09:00", false);
    }

    @Test
    void 정상_청크는_통과한다() {
        assertThatCode(() -> GpsSessionValidator.validate(1L,
                chunk(List.of(point(37.5908521, 126.704748), point(37.5908142, 126.7048013)), false)))
                .doesNotThrowAnyException();
    }

    @Test
    void finished_이면서_좌표가_없어도_통과한다() {
        assertThatCode(() -> GpsSessionValidator.validate(1L, chunk(List.of(), true)))
                .doesNotThrowAnyException();
    }

    @Test
    void 좌표가_없고_finished도_아니면_거부한다() {
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, chunk(List.of(), false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uno가_없으면_거부한다() {
        assertThatThrownBy(() -> GpsSessionValidator.validate(null,
                chunk(List.of(point(37.0, 127.0)), false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void clientRunId가_비면_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest("  ", "RUNNING",
                List.of(point(37.0, 127.0)), false, null, null, 1, null, false);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void RUNNING이_아닌_활동은_거부한다() {
        GpsSessionRequest req = new GpsSessionRequest("run-1", "CYCLING",
                List.of(point(37.0, 127.0)), false, null, null, 1, null, false);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void activityType이_없으면_RUNNING으로_간주해_통과한다() {
        GpsSessionRequest req = new GpsSessionRequest("run-1", null,
                List.of(point(37.0, 127.0)), false, null, null, 1, null, false);
        assertThatCode(() -> GpsSessionValidator.validate(1L, req)).doesNotThrowAnyException();
    }

    @Test
    void 좌표범위를_벗어나면_거부한다() {
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L,
                chunk(List.of(point(91.0, 127.0)), false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordedAt이_없는_좌표는_거부한다() {
        GpsSessionRequest.GpsPoint noTime =
                new GpsSessionRequest.GpsPoint(0, null, 37.0, 127.0, null, null, null, null);
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, chunk(List.of(noTime), false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 한_청크_좌표수_상한을_넘으면_거부한다() {
        List<GpsSessionRequest.GpsPoint> tooMany = java.util.stream.IntStream
                .range(0, GpsSessionValidator.MAX_CHUNK_POINTS + 1)
                .mapToObj(i -> point(37.0, 127.0))
                .toList();
        assertThatThrownBy(() -> GpsSessionValidator.validate(1L, chunk(tooMany, false)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

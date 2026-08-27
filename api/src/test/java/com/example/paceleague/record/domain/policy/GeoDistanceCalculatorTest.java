package com.example.paceleague.record.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoDistanceCalculatorTest {

    @Test
    void 같은_좌표는_거리가_0이다() {
        assertThat(GeoDistanceCalculator.haversineMeters(37.5, 127.0, 37.5, 127.0)).isZero();
    }

    @Test
    void 위도_1도_차이는_약_111km다() {
        double d = GeoDistanceCalculator.haversineMeters(37.0, 127.0, 38.0, 127.0);
        assertThat(d).isBetween(110_000.0, 112_000.0);
    }

    @Test
    void 짧은_구간_거리를_미터_단위로_계산한다() {
        // 서울시청 부근 두 점, 실제 약 90m 안팎
        double d = GeoDistanceCalculator.haversineMeters(37.5665, 126.9780, 37.5673, 126.9780);
        assertThat(d).isBetween(80.0, 100.0);
    }

    @Test
    void 대칭이다() {
        double ab = GeoDistanceCalculator.haversineMeters(37.5908521, 126.704748, 37.5908142, 126.7048013);
        double ba = GeoDistanceCalculator.haversineMeters(37.5908142, 126.7048013, 37.5908521, 126.704748);
        assertThat(ab).isEqualTo(ba);
    }
}

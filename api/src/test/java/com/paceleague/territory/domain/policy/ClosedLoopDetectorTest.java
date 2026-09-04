package com.paceleague.territory.domain.policy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClosedLoopDetectorTest {

    private static final double LAT0 = 37.5;
    private static final double LNG0 = 127.0;
    private static final double D = 0.002; // 약 200m 남짓

    @Test
    void 시작과_끝이_붙은_경로는_닫힌_루프다() {
        List<double[]> path = List.of(
                new double[]{LAT0, LNG0},
                new double[]{LAT0, LNG0 + D},
                new double[]{LAT0 + D, LNG0 + D},
                new double[]{LAT0 + D, LNG0},
                new double[]{LAT0, LNG0}
        );
        assertThat(ClosedLoopDetector.isClosedLoop(path, 50.0)).isTrue();
    }

    @Test
    void 시작과_끝이_임계값보다_멀면_닫힌_루프가_아니다() {
        List<double[]> path = List.of(
                new double[]{LAT0, LNG0},
                new double[]{LAT0, LNG0 + D},
                new double[]{LAT0 + D, LNG0 + D},
                new double[]{LAT0 + D, LNG0}      // 시작점으로 안 돌아옴(약 200m 떨어짐)
        );
        assertThat(ClosedLoopDetector.isClosedLoop(path, 50.0)).isFalse();
    }

    @Test
    void 임계값을_키우면_같은_경로도_닫힌_루프로_인정된다() {
        List<double[]> path = List.of(
                new double[]{LAT0, LNG0},
                new double[]{LAT0, LNG0 + D},
                new double[]{LAT0 + D, LNG0 + D},
                new double[]{LAT0 + D, LNG0}
        );
        assertThat(ClosedLoopDetector.isClosedLoop(path, 300.0)).isTrue();
    }

    @Test
    void 서로_다른_점이_3개_미만이면_닫힌_루프가_아니다() {
        List<double[]> path = List.of(
                new double[]{LAT0, LNG0},
                new double[]{LAT0, LNG0},
                new double[]{LAT0, LNG0 + D}
        );
        assertThat(ClosedLoopDetector.isClosedLoop(path, 50.0)).isFalse();
    }

    @Test
    void null이면_닫힌_루프가_아니다() {
        assertThat(ClosedLoopDetector.isClosedLoop(null, 50.0)).isFalse();
    }
}

package com.paceleague.territory.domain.policy;

import com.uber.h3core.H3Core;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// H3Core는 실제 네이티브 인스턴스를 쓴다 — ProcessTerritoryRunServiceTest와 같은 이유(실제 변환 경로 검증).
class H3TerritoryGridTest {

    private H3Core h3Core;

    private static final List<double[]> SQUARE = List.of(
            new double[]{37.5, 127.0}, new double[]{37.5, 127.002},
            new double[]{37.502, 127.002}, new double[]{37.502, 127.0},
            new double[]{37.5, 127.0});

    @BeforeEach
    void setUp() throws Exception {
        h3Core = H3Core.newInstance();
    }

    @Test
    void 헥사곤_집합을_덮으면_각_셀의_경계가_닫힌_링으로_반환된다() {
        List<Long> covered = H3TerritoryGrid.coverRing(h3Core, SQUARE, 12);

        List<List<double[]>> boundaries = H3TerritoryGrid.cellBoundariesLatLng(h3Core, covered);

        assertThat(boundaries).hasSize(covered.size());
        for (List<double[]> ring : boundaries) {
            assertThat(ring.size()).isGreaterThanOrEqualTo(6); // 육각형(드물게 오각형) + 닫힘 좌표
            double[] first = ring.get(0);
            double[] last = ring.get(ring.size() - 1);
            assertThat(first[0]).isEqualTo(last[0]);
            assertThat(first[1]).isEqualTo(last[1]);
        }
    }

    @Test
    void 빈_인덱스_목록이면_빈_결과를_반환한다() {
        List<List<double[]>> boundaries = H3TerritoryGrid.cellBoundariesLatLng(h3Core, List.of());

        assertThat(boundaries).isEmpty();
    }
}

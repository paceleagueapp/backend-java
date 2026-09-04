package com.paceleague.territory.domain.policy;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolygonGeometryTest {

    // 위도 37.5 부근에서 약 100m x 100m 정사각형(면적 ~10,000㎡, 둘레 ~400m).
    private static final double LAT0 = 37.5;
    private static final double LNG0 = 127.0;
    private static final double D_LAT_100M = 100.0 / 111_320.0;
    private static final double D_LNG_100M = 100.0 / (111_320.0 * Math.cos(Math.toRadians(LAT0)));

    private static List<double[]> square(double lat, double lng, double dLat, double dLng) {
        List<double[]> ring = new ArrayList<>();
        ring.add(new double[]{lat, lng});
        ring.add(new double[]{lat, lng + dLng});
        ring.add(new double[]{lat + dLat, lng + dLng});
        ring.add(new double[]{lat + dLat, lng});
        ring.add(new double[]{lat, lng});
        return ring;
    }

    @Test
    void 정사각형_면적을_제곱미터로_계산한다() {
        PolygonGeometry poly = PolygonGeometry.fromLatLngRing(square(LAT0, LNG0, D_LAT_100M, D_LNG_100M));
        assertThat(poly.areaSqm()).isBetween(9_500.0, 10_500.0);
    }

    @Test
    void 정사각형_둘레를_미터로_계산한다() {
        PolygonGeometry poly = PolygonGeometry.fromLatLngRing(square(LAT0, LNG0, D_LAT_100M, D_LNG_100M));
        assertThat(poly.perimeterMeters()).isBetween(390.0, 410.0);
    }

    @Test
    void bbox는_입력_좌표의_최소최대다() {
        PolygonGeometry poly = PolygonGeometry.fromLatLngRing(square(LAT0, LNG0, D_LAT_100M, D_LNG_100M));
        double[] bbox = poly.bboxLatLng();
        assertThat(bbox[0]).isEqualTo(LAT0);
        assertThat(bbox[1]).isEqualTo(LNG0);
        assertThat(bbox[2]).isCloseTo(LAT0 + D_LAT_100M, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(bbox[3]).isCloseTo(LNG0 + D_LNG_100M, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void 중심은_정사각형_한가운데다() {
        PolygonGeometry poly = PolygonGeometry.fromLatLngRing(square(LAT0, LNG0, D_LAT_100M, D_LNG_100M));
        double[] c = poly.centroidLatLng();
        assertThat(c[0]).isCloseTo(LAT0 + D_LAT_100M / 2, org.assertj.core.data.Offset.offset(1e-5));
        assertThat(c[1]).isCloseTo(LNG0 + D_LNG_100M / 2, org.assertj.core.data.Offset.offset(1e-5));
    }

    @Test
    void 절반_겹치는_두_사각형의_교집합_면적은_전체의_약_절반이다() {
        PolygonGeometry a = PolygonGeometry.fromLatLngRing(square(LAT0, LNG0, D_LAT_100M, D_LNG_100M));
        // 경도로 절반 밀어 겹침
        List<double[]> shifted = square(LAT0, LNG0 + D_LNG_100M / 2, D_LAT_100M, D_LNG_100M);
        assertThat(a.intersectionAreaSqm(shifted)).isBetween(4_500.0, 5_500.0);
    }

    @Test
    void 완전히_떨어진_사각형과는_교집합이_0이다() {
        PolygonGeometry a = PolygonGeometry.fromLatLngRing(square(LAT0, LNG0, D_LAT_100M, D_LNG_100M));
        List<double[]> far = square(LAT0 + 1.0, LNG0 + 1.0, D_LAT_100M, D_LNG_100M);
        assertThat(a.intersectionAreaSqm(far)).isZero();
    }

    @Test
    void 점이_3개_미만이면_예외다() {
        assertThatThrownBy(() -> PolygonGeometry.fromLatLngRing(List.of(
                new double[]{LAT0, LNG0}, new double[]{LAT0, LNG0 + D_LNG_100M})))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

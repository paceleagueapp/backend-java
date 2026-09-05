package com.paceleague.territory.domain.policy;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

// 러닝 GPS 좌표 링(위/경도)으로 만든 다각형의 면적·둘레·bbox·중심·교집합 면적을 계산하는 순수 로직.
// 프로젝트에 공간 DB 타입을 도입하지 않고(record_track.points_json과 동일한 방침) Java에서 계산한다.
//
// 위/경도는 bbox 중심을 기준점으로 한 등거리 근사 평면(미터)으로 투영한 뒤 JTS로 계산한다 —
// 수 km 규모의 러닝 영역에서는 오차가 무시할 수준. 자기교차(figure-8) 링은 buffer(0)으로 정규화해
// 유효 다각형(들)의 합으로 다룬다.
public final class PolygonGeometry {

    // 위도 1도 ≈ 111.32km. 경도 1도는 위도에 따라 cos배로 줄어든다.
    private static final double METERS_PER_DEG_LAT = 111_320.0;
    private static final GeometryFactory GF = new GeometryFactory();

    private final List<double[]> ring;   // 닫힌 위/경도 링 [[lat,lng], ...]
    private final double refLat;
    private final double refLng;
    private final Geometry geometry;     // 투영(미터) + buffer(0) 정규화된 지오메트리
    private final double minLat;
    private final double minLng;
    private final double maxLat;
    private final double maxLng;

    private PolygonGeometry(List<double[]> ring, double refLat, double refLng, Geometry geometry,
                            double minLat, double minLng, double maxLat, double maxLng) {
        this.ring = ring;
        this.refLat = refLat;
        this.refLng = refLng;
        this.geometry = geometry;
        this.minLat = minLat;
        this.minLng = minLng;
        this.maxLat = maxLat;
        this.maxLng = maxLng;
    }

    public static PolygonGeometry fromLatLngRing(List<double[]> latLngPoints) {
        List<double[]> closed = close(latLngPoints);
        if (closed.size() < 4) {
            throw new IllegalArgumentException("polygon ring needs at least 3 distinct points");
        }

        double minLat = Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        for (double[] p : closed) {
            minLat = Math.min(minLat, p[0]);
            maxLat = Math.max(maxLat, p[0]);
            minLng = Math.min(minLng, p[1]);
            maxLng = Math.max(maxLng, p[1]);
        }

        double refLat = (minLat + maxLat) / 2.0;
        double refLng = (minLng + maxLng) / 2.0;
        Geometry geometry = normalize(buildProjected(closed, refLat, refLng));
        return new PolygonGeometry(closed, refLat, refLng, geometry, minLat, minLng, maxLat, maxLng);
    }

    public double areaSqm() {
        return geometry.getArea();
    }

    public double perimeterMeters() {
        return geometry.getLength();
    }

    // [minLat, minLng, maxLat, maxLng]
    public double[] bboxLatLng() {
        return new double[]{minLat, minLng, maxLat, maxLng};
    }

    // [lat, lng]
    public double[] centroidLatLng() {
        Point centroid = geometry.getCentroid();
        return unproject(centroid.getX(), centroid.getY(), refLat, refLng);
    }

    public List<double[]> ring() {
        return ring;
    }

    // 다른 위/경도 링과의 교집합 면적(㎡). 두 링을 이 다각형의 기준점으로 함께 투영해 계산한다.
    public double intersectionAreaSqm(List<double[]> otherLatLngRing) {
        try {
            List<double[]> otherClosed = close(otherLatLngRing);
            if (otherClosed.size() < 4) {
                return 0.0;
            }
            Geometry other = normalize(buildProjected(otherClosed, refLat, refLng));
            return geometry.intersection(other).getArea();
        } catch (RuntimeException e) {
            // JTS TopologyException 등 — 겹침 없음으로 처리
            return 0.0;
        }
    }

    private static Geometry normalize(Polygon polygon) {
        return polygon.isValid() ? polygon : polygon.buffer(0);
    }

    private static Polygon buildProjected(List<double[]> closedLatLngRing, double refLat, double refLng) {
        Coordinate[] coords = new Coordinate[closedLatLngRing.size()];
        for (int i = 0; i < closedLatLngRing.size(); i++) {
            double[] latLng = closedLatLngRing.get(i);
            double[] xy = project(latLng[0], latLng[1], refLat, refLng);
            coords[i] = new Coordinate(xy[0], xy[1]);
        }
        return GF.createPolygon(coords);
    }

    private static List<double[]> close(List<double[]> latLngPoints) {
        List<double[]> out = new ArrayList<>();
        double[] prev = null;
        for (double[] p : latLngPoints) {
            if (prev != null && prev[0] == p[0] && prev[1] == p[1]) {
                continue; // 연속 중복 좌표 제거
            }
            out.add(new double[]{p[0], p[1]});
            prev = p;
        }
        if (out.size() >= 2) {
            double[] first = out.get(0);
            double[] last = out.get(out.size() - 1);
            if (first[0] != last[0] || first[1] != last[1]) {
                out.add(new double[]{first[0], first[1]});
            }
        }
        return out;
    }

    private static double metersPerDegLng(double refLat) {
        return METERS_PER_DEG_LAT * Math.cos(Math.toRadians(refLat));
    }

    // package-private: H3TerritoryGrid가 헥사곤 경계를 같은 등거리 평면에 투영/역투영할 때 재사용한다.
    static double[] project(double lat, double lng, double refLat, double refLng) {
        double x = (lng - refLng) * metersPerDegLng(refLat);
        double y = (lat - refLat) * METERS_PER_DEG_LAT;
        return new double[]{x, y};
    }

    static double[] unproject(double x, double y, double refLat, double refLng) {
        double lat = refLat + y / METERS_PER_DEG_LAT;
        double lng = refLng + x / metersPerDegLng(refLat);
        return new double[]{lat, lng};
    }
}

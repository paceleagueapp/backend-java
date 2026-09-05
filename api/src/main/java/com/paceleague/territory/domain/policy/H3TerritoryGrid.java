package com.paceleague.territory.domain.policy;

import com.uber.h3core.AreaUnit;
import com.uber.h3core.H3Core;
import com.uber.h3core.PolygonToCellsFlags;
import com.uber.h3core.util.LatLng;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;

// 러닝 도형(위/경도 링)을 H3 헥사곤 집합으로 바꾸고, 그 집합의 면적/외곽선을 계산하는 순수 로직.
// H3Core는 호출부(ProcessTerritoryRunService)가 Spring 빈으로 주입받아 파라미터로 넘긴다 —
// 네이티브 라이브러리 로딩(IOException)이 필요해 PolygonGeometry처럼 완전히 상태 없는 정적 유틸로만
// 두기 어렵기 때문. GF/투영 방식은 PolygonGeometry와 동일(등거리 근사 평면 + JTS).
public final class H3TerritoryGrid {

    private static final GeometryFactory GF = new GeometryFactory();

    private H3TerritoryGrid() {
    }

    // ring: PolygonGeometry.ring()이 돌려주는 닫힌 위/경도 링. CONTAINMENT_OVERLAPPING 모드로
    // 도형 안에 완전히 포함되거나 경계에 걸쳐진 헥사곤까지 전부 반환한다.
    public static List<Long> coverRing(H3Core h3, List<double[]> latLngRing, int resolution) {
        List<LatLng> points = new ArrayList<>(latLngRing.size());
        for (double[] p : latLngRing) {
            points.add(new LatLng(p[0], p[1]));
        }
        return h3.polygonToCellsExperimental(points, List.of(), resolution,
                PolygonToCellsFlags.containment_overlapping);
    }

    public static double totalAreaSqm(H3Core h3, List<Long> hexIndexes) {
        double sum = 0.0;
        for (long idx : hexIndexes) {
            sum += h3.cellArea(idx, AreaUnit.m2);
        }
        return sum;
    }

    // 소유 헥사곤 집합의 합집합 외곽선을 위/경도 링으로 반환 — 지도에 그대로 그린다.
    // 여러 조각으로 갈라지는 경우(드묾)는 가장 넓은 조각만 취한다.
    public static List<double[]> unionBoundaryLatLng(H3Core h3, List<Long> hexIndexes) {
        if (hexIndexes.isEmpty()) {
            return List.of();
        }

        List<List<LatLng>> boundaries = new ArrayList<>(hexIndexes.size());
        double sumLat = 0.0;
        double sumLng = 0.0;
        int pointCount = 0;
        for (long idx : hexIndexes) {
            List<LatLng> boundary = h3.cellToBoundary(idx);
            boundaries.add(boundary);
            for (LatLng p : boundary) {
                sumLat += p.lat;
                sumLng += p.lng;
                pointCount++;
            }
        }
        double refLat = sumLat / pointCount;
        double refLng = sumLng / pointCount;

        List<Geometry> cells = new ArrayList<>(boundaries.size());
        for (List<LatLng> boundary : boundaries) {
            Coordinate[] coords = new Coordinate[boundary.size() + 1];
            for (int i = 0; i < boundary.size(); i++) {
                LatLng p = boundary.get(i);
                double[] xy = PolygonGeometry.project(p.lat, p.lng, refLat, refLng);
                coords[i] = new Coordinate(xy[0], xy[1]);
            }
            coords[boundary.size()] = coords[0];
            cells.add(GF.createPolygon(coords));
        }

        Polygon largest = largestPolygon(UnaryUnionOp.union(cells));
        Coordinate[] exterior = largest.getExteriorRing().getCoordinates();
        List<double[]> ring = new ArrayList<>(exterior.length);
        for (Coordinate c : exterior) {
            ring.add(PolygonGeometry.unproject(c.x, c.y, refLat, refLng));
        }
        return ring;
    }

    // 헥사곤 하나하나의 경계를 위/경도 링으로 반환 — 지도 확대 시 격자 모양을 그대로 그리기 위함
    // (unionBoundaryLatLng는 합쳐진 외곽선 하나만 주는 것과 대조적으로, 이건 셀별 개별 링 목록).
    public static List<List<double[]>> cellBoundariesLatLng(H3Core h3, List<Long> hexIndexes) {
        List<List<double[]>> out = new ArrayList<>(hexIndexes.size());
        for (long idx : hexIndexes) {
            List<LatLng> boundary = h3.cellToBoundary(idx);
            List<double[]> ring = new ArrayList<>(boundary.size() + 1);
            for (LatLng p : boundary) {
                ring.add(new double[]{p.lat, p.lng});
            }
            if (!ring.isEmpty()) {
                ring.add(ring.get(0)); // 다른 ring들과 동일하게 닫힌 링으로 맞춘다
            }
            out.add(ring);
        }
        return out;
    }

    private static Polygon largestPolygon(Geometry geometry) {
        if (geometry instanceof Polygon polygon) {
            return polygon;
        }
        Polygon best = null;
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            if (geometry.getGeometryN(i) instanceof Polygon candidate
                    && (best == null || candidate.getArea() > best.getArea())) {
                best = candidate;
            }
        }
        return best;
    }
}

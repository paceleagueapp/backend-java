package com.paceleague.record.domain.policy;

// 두 위경도 사이 대권거리(great-circle distance)를 미터로 계산하는 순수 로직 — haversine 공식.
// GPS 좌표는 앱에서 이미 Kalman/정확도 필터를 거쳐 들어오므로(payload의 algorithmVersion 참고)
// 여기서 추가 필터링은 하지 않고, 연속한 좌표 간 거리를 그대로 누적한다.
public final class GeoDistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private GeoDistanceCalculator() {}

    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}

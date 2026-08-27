package com.example.paceleague.territory.domain.policy;

import com.example.paceleague.record.domain.policy.GeoDistanceCalculator;

import java.util.ArrayList;
import java.util.List;

// 러닝 GPS 경로가 "닫힌 도형"을 이루는지 판정하는 순수 로직.
// 시작점과 끝점이 close-threshold-meters 이내로 가까우면 닫힌 루프로 본다(마지막→처음을 이어 폐곡선으로 보정).
//
// record.domain.policy.GeoDistanceCalculator(haversine, 순수 static)를 그대로 재사용한다 —
// board.BoardQueryServiceImpl이 rank.domain.policy.RankTierLabelPolicy를 포트 없이 직접 부르는 것과
// 같은 예외(상태 없는 순수 조회/계산 정책의 도메인 간 직접 호출). docs/architecture.md 참고.
public final class ClosedLoopDetector {

    private ClosedLoopDetector() {
    }

    // latLngPoints: [[lat,lng], ...] 러닝 경로 좌표(시간순)
    public static boolean isClosedLoop(List<double[]> latLngPoints, double thresholdMeters) {
        if (latLngPoints == null) {
            return false;
        }
        List<double[]> distinct = dedupeConsecutive(latLngPoints);
        if (distinct.size() < 3) {
            return false;
        }
        double[] first = distinct.get(0);
        double[] last = distinct.get(distinct.size() - 1);
        double gap = GeoDistanceCalculator.haversineMeters(first[0], first[1], last[0], last[1]);
        return gap <= thresholdMeters;
    }

    private static List<double[]> dedupeConsecutive(List<double[]> points) {
        List<double[]> out = new ArrayList<>();
        double[] prev = null;
        for (double[] p : points) {
            if (p == null || p.length < 2) {
                continue;
            }
            if (prev != null && prev[0] == p[0] && prev[1] == p[1]) {
                continue;
            }
            out.add(p);
            prev = p;
        }
        return out;
    }
}

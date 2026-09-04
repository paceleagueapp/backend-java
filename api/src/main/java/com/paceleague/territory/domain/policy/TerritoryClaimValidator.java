package com.paceleague.territory.domain.policy;

// 닫힌 도형이 "땅"으로 인정될 수 있는지 검증하는 순수 로직.
// 너무 짧거나 작은 도형(GPS 오차로 생기는 자잘한 루프), 비정상적으로 큰 도형은 제외한다.
// 실패는 예외가 아니라 boolean으로 돌려준다 — 땅 판정 실패는 러너 입장에서 정상 상황이므로.
public final class TerritoryClaimValidator {

    private TerritoryClaimValidator() {
    }

    public static boolean isClaimable(double perimeterMeters, double areaSqm,
                                      double minPerimeterMeters, double minAreaSqm, double maxAreaSqm) {
        if (perimeterMeters < minPerimeterMeters) {
            return false;
        }
        if (areaSqm < minAreaSqm) {
            return false;
        }
        return areaSqm <= maxAreaSqm;
    }
}

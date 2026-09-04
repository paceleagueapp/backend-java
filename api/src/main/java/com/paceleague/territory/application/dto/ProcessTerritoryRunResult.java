package com.paceleague.territory.application.dto;

import java.util.List;

// 땅따먹기 러닝 처리 결과. 러닝 종료 응답(GpsSessionResponse.territoryResult)으로도 그대로 노출되어
// 앱이 "새 땅 점령!" / "OO님 땅을 뺏었습니다" 같은 피드백을 보여줄 수 있다.
//  - NO_LOOP: 닫힌 도형이 아님(러너 입장에서 정상)
//  - INVALID_SHAPE: 너무 작거나 큰 도형이라 땅으로 인정 안 됨
//  - CREATED: 빈 구역이라 새 땅 생성(createdTerritorySno)
//  - INTERACTED: 기존 땅과 겹쳐 데미지/회복/점령 발생
public record ProcessTerritoryRunResult(
        Outcome outcome,
        Long createdTerritorySno,
        List<CapturedTerritory> capturedTerritories,
        List<Long> damagedTerritorySnos,
        List<Long> healedTerritorySnos
) {
    public enum Outcome {
        NO_LOOP, INVALID_SHAPE, CREATED, INTERACTED
    }

    // HP를 0으로 만들어 이번 러닝으로 점령한 남의 땅 1건.
    public record CapturedTerritory(
            Long territorySno,
            Long previousOwnerMemberSno,
            String previousOwnerNickname
    ) {
    }

    public static ProcessTerritoryRunResult noLoop() {
        return new ProcessTerritoryRunResult(Outcome.NO_LOOP, null, List.of(), List.of(), List.of());
    }

    public static ProcessTerritoryRunResult invalidShape() {
        return new ProcessTerritoryRunResult(Outcome.INVALID_SHAPE, null, List.of(), List.of(), List.of());
    }

    public static ProcessTerritoryRunResult created(Long territorySno) {
        return new ProcessTerritoryRunResult(Outcome.CREATED, territorySno, List.of(), List.of(), List.of());
    }

    public static ProcessTerritoryRunResult interacted(List<CapturedTerritory> captured,
                                                       List<Long> damaged, List<Long> healed) {
        return new ProcessTerritoryRunResult(Outcome.INTERACTED, null, captured, damaged, healed);
    }
}

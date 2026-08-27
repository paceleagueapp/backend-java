package com.example.paceleague.territory.application.dto;

import java.util.List;

// 땅따먹기 러닝 처리 결과. 주로 로깅/테스트 검증용.
//  - NO_LOOP: 닫힌 도형이 아님(러너 입장에서 정상)
//  - INVALID_SHAPE: 너무 작거나 큰 도형이라 땅으로 인정 안 됨
//  - CREATED: 빈 구역이라 새 땅 생성
//  - INTERACTED: 기존 땅과 겹쳐 데미지/회복/점령 발생
public record ProcessTerritoryRunResult(
        Outcome outcome,
        Long createdTerritorySno,
        List<Long> damagedTerritorySnos,
        List<Long> capturedTerritorySnos,
        List<Long> healedTerritorySnos
) {
    public enum Outcome {
        NO_LOOP, INVALID_SHAPE, CREATED, INTERACTED
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

    public static ProcessTerritoryRunResult interacted(List<Long> damaged, List<Long> captured, List<Long> healed) {
        return new ProcessTerritoryRunResult(Outcome.INTERACTED, null, damaged, captured, healed);
    }
}

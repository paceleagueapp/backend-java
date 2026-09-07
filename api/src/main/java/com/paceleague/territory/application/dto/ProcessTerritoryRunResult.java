package com.paceleague.territory.application.dto;

import java.util.List;

// 땅따먹기 러닝 처리 결과. 러닝 종료 응답(GpsSessionResponse.territoryResult)으로도 그대로 노출되어
// 앱이 "새 땅 점령!" / "OO님 땅을 뺏었습니다" 같은 피드백을 보여줄 수 있다.
//  - NO_LOOP: 닫힌 도형이 아님(러너 입장에서 정상)
//  - INVALID_SHAPE: 너무 작거나 큰 도형이라 땅으로 인정 안 됨
//  - CREATED: 완전히 빈 구역이라 새 땅 생성(createdTerritorySno), 겹침이 아예 없었던 경우.
//  - INTERACTED: 기존 땅과 헥사곤이 겹침(2026-09-07: 겹친 헥사곤만 점령). capturedTerritories에 뺏은 대상이
//    담기고, 이번 러닝이 빈 헥사곤이나 뺏은 헥사곤으로 새 territory를 만들었으면 createdTerritorySno도
//    함께 채워진다 — CREATED와 달리 이 필드가 non-null이어도 Outcome은 INTERACTED일 수 있다(둘 다 일어난
//    경우를 별도 Outcome 없이 표현). damagedTerritorySnos/healedTerritorySnos는 HP 제거(2026-09-05) 이후
//    항상 빈 리스트 — 앱 계약을 바꾸지 않기 위해 필드만 유지.
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

    public static ProcessTerritoryRunResult interacted(Long createdTerritorySno, List<CapturedTerritory> captured,
                                                       List<Long> damaged, List<Long> healed) {
        return new ProcessTerritoryRunResult(Outcome.INTERACTED, createdTerritorySno, captured, damaged, healed);
    }
}

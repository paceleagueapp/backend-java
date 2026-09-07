package com.paceleague.territory.application.dto;

// territory 재생(historical replay, 2026-09-07) 1회 실행 결과 요약. 로그로만 확인하므로 API로는 안 나간다.
public record TerritoryReplaySummary(
        int totalRuns,
        int created,
        int interacted,
        int skipped, // NO_LOOP / INVALID_SHAPE / 좌표 파싱 실패
        int failed   // process() 호출 자체가 예외를 던진 경우
) {
}

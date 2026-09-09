package com.paceleague.territory.adapter.out.persistence;

// 일간 요약 푸시용 집계 — create_at 구간 내 territory 개수 / 서로 다른 소유자 수.
public interface TerritoryCaptureCountProjection {
    Long getCapturedTerritories();

    Long getDistinctOwners();
}

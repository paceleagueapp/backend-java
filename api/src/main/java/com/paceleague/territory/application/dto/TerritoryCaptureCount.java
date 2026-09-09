package com.paceleague.territory.application.dto;

// 일간 요약 푸시용 — 특정 구간에 점령된 땅 수 / 그 소유자(유저) 수.
public record TerritoryCaptureCount(
        long capturedTerritories,
        long distinctOwners
) {
}

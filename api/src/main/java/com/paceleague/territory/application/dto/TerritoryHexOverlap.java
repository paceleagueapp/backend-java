package com.paceleague.territory.application.dto;

// 이번 러닝이 덮은 헥사곤 집합이 어느 ACTIVE territory와 몇 개나 겹치는지 — territory_sno별 집계 1건.
public record TerritoryHexOverlap(Long territorySno, long overlapHexCount) {
}

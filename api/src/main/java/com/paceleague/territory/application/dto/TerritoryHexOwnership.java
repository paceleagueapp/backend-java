package com.paceleague.territory.application.dto;

// 이번 러닝이 덮은 헥사곤 하나가 이미 어느 ACTIVE territory에 속해 있는지 — 헥사곤 단위 1건.
// (겹침을 territory 단위 집계가 아니라 헥사곤 단위로 다뤄야 "겹친 부분만" 뺏을 수 있다.)
public record TerritoryHexOwnership(Long h3Index, Long territorySno) {
}

package com.paceleague.territory.application.dto;

// 소유자별 총 점령 면적 집계 결과(포트 반환용). 어댑터가 네이티브 프로젝션을 이 레코드로 변환한다.
public record TerritoryOwnerArea(
        Long ownerMemberSno,
        double totalAreaSqm,
        long territoryCount
) {
}

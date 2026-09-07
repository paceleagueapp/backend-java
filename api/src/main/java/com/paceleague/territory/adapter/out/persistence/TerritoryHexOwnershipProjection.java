package com.paceleague.territory.adapter.out.persistence;

// 헥사곤 단위 소유 조회 네이티브 쿼리 결과 프로젝션.
public interface TerritoryHexOwnershipProjection {
    Long getH3Index();

    Long getTerritorySno();
}

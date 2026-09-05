package com.paceleague.territory.adapter.out.persistence;

// 헥사곤 겹침 집계 네이티브 쿼리 결과 프로젝션.
public interface TerritoryHexOverlapProjection {
    Long getTerritorySno();

    Long getOverlapHexCount();
}

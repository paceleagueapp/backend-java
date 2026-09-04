package com.paceleague.territory.adapter.out.persistence;

// 소유자별 총 점령 면적 집계 — 땅따먹기 면적 랭킹용.
public interface TerritoryOwnerAreaProjection {
    Long getOwnerMemberSno();

    Double getTotalAreaSqm();

    Long getTerritoryCount();
}

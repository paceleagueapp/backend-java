package com.paceleague.territory.application.port.out;

import com.paceleague.territory.application.dto.TerritoryHexOverlap;
import com.paceleague.territory.domain.entity.TerritoryHex;

import java.util.List;

public interface TerritoryHexRepositoryPort {

    void saveAll(List<TerritoryHex> hexes);

    // 이번 러닝이 덮은 헥사곤 인덱스들이 어느 ACTIVE territory와 몇 개나 겹치는지 territory_sno별로 집계.
    List<TerritoryHexOverlap> findActiveOverlapCounts(List<Long> h3Indexes);

    // 후보 헥사곤 인덱스 중 이미 어딘가에 배정된 것만 반환 — 백필 시 선점된 셀을 건너뛰기 위함.
    List<Long> findExistingIndexes(List<Long> h3Indexes);

    // 지도 상세(헥사곤 격자) 렌더링용 — 여러 territory의 소유 헥사곤을 한 번에 조회.
    List<TerritoryHex> findByTerritorySnoIn(List<Long> territorySnos);
}

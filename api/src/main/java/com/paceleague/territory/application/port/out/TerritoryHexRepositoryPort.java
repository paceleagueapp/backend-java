package com.paceleague.territory.application.port.out;

import com.paceleague.territory.application.dto.TerritoryHexOverlap;
import com.paceleague.territory.domain.entity.TerritoryHex;

import java.util.List;

public interface TerritoryHexRepositoryPort {

    void saveAll(List<TerritoryHex> hexes);

    // 이번 러닝이 덮은 헥사곤 인덱스들이 어느 ACTIVE territory와 몇 개나 겹치는지 territory_sno별로 집계.
    List<TerritoryHexOverlap> findActiveOverlapCounts(List<Long> h3Indexes);
}

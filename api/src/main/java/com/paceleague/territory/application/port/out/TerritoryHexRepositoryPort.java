package com.paceleague.territory.application.port.out;

import com.paceleague.territory.application.dto.TerritoryHexOwnership;
import com.paceleague.territory.domain.entity.TerritoryHex;

import java.util.List;

public interface TerritoryHexRepositoryPort {

    void saveAll(List<TerritoryHex> hexes);

    // 이번 러닝이 덮은 헥사곤 인덱스들 중 이미 어느 ACTIVE territory가 소유한 것을 헥사곤 단위로 반환
    // (겹친 부분만 뺏기 위해 territory 단위 집계가 아니라 헥사곤 하나하나의 소유자를 알아야 한다).
    List<TerritoryHexOwnership> findActiveOwners(List<Long> h3Indexes);

    // 후보 헥사곤 인덱스 중 이미 어딘가에 배정된 것만 반환 — 백필 시 선점된 셀을 건너뛰기 위함.
    List<Long> findExistingIndexes(List<Long> h3Indexes);

    // 지도 상세(헥사곤 격자) 렌더링용 — 여러 territory의 소유 헥사곤을 한 번에 조회.
    List<TerritoryHex> findByTerritorySnoIn(List<Long> territorySnos);
}

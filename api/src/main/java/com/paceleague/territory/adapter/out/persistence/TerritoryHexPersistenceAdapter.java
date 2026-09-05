package com.paceleague.territory.adapter.out.persistence;

import com.paceleague.territory.application.dto.TerritoryHexOverlap;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.domain.entity.TerritoryHex;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TerritoryHexPersistenceAdapter implements TerritoryHexRepositoryPort {

    private final TerritoryHexJpaRepository territoryHexJpaRepository;

    @Override
    public void saveAll(List<TerritoryHex> hexes) {
        territoryHexJpaRepository.saveAll(hexes);
    }

    @Override
    public List<TerritoryHexOverlap> findActiveOverlapCounts(List<Long> h3Indexes) {
        return territoryHexJpaRepository.findActiveOverlapCounts(h3Indexes).stream()
                .map(p -> new TerritoryHexOverlap(p.getTerritorySno(), p.getOverlapHexCount()))
                .toList();
    }

    @Override
    public List<Long> findExistingIndexes(List<Long> h3Indexes) {
        return territoryHexJpaRepository.findAllById(h3Indexes).stream()
                .map(TerritoryHex::getH3Index)
                .toList();
    }

    @Override
    public List<TerritoryHex> findByTerritorySnoIn(List<Long> territorySnos) {
        return territoryHexJpaRepository.findByTerritorySnoIn(territorySnos);
    }
}

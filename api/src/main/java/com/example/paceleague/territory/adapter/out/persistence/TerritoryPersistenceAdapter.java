package com.example.paceleague.territory.adapter.out.persistence;

import com.example.paceleague.territory.application.dto.TerritoryOwnerArea;
import com.example.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.example.paceleague.territory.domain.entity.Territory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TerritoryPersistenceAdapter implements TerritoryRepositoryPort {

    private final TerritoryJpaRepository territoryJpaRepository;

    public Territory save(Territory territory) {
        return territoryJpaRepository.save(territory);
    }

    public Optional<Territory> findBySno(Long sno) {
        return territoryJpaRepository.findById(sno);
    }

    public List<Territory> findActiveIntersectingBbox(BigDecimal minLat, BigDecimal minLng,
                                                     BigDecimal maxLat, BigDecimal maxLng, int limit) {
        return territoryJpaRepository.findActiveIntersectingBbox(
                minLat, minLng, maxLat, maxLng, PageRequest.of(0, limit));
    }

    public List<Territory> findActiveIntersectingBboxForUpdate(BigDecimal minLat, BigDecimal minLng,
                                                              BigDecimal maxLat, BigDecimal maxLng) {
        return territoryJpaRepository.findActiveIntersectingBboxForUpdate(minLat, minLng, maxLat, maxLng);
    }

    public List<TerritoryOwnerArea> findTopOwnersByArea(int limit) {
        return territoryJpaRepository.findTopOwnersByArea(limit).stream()
                .map(p -> new TerritoryOwnerArea(
                        p.getOwnerMemberSno(),
                        p.getTotalAreaSqm() == null ? 0.0 : p.getTotalAreaSqm(),
                        p.getTerritoryCount() == null ? 0L : p.getTerritoryCount()))
                .toList();
    }
}

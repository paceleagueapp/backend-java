package com.paceleague.territory.adapter.out.persistence;

import com.paceleague.territory.application.dto.TerritoryCaptureCount;
import com.paceleague.territory.application.dto.TerritoryOwnerArea;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.domain.entity.Territory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TerritoryPersistenceAdapter implements TerritoryRepositoryPort {

    private final TerritoryJpaRepository territoryJpaRepository;

    public Territory save(Territory territory) {
        return territoryJpaRepository.save(territory);
    }

    public void delete(Territory territory) {
        territoryJpaRepository.delete(territory);
    }

    public Optional<Territory> findBySno(Long sno) {
        return territoryJpaRepository.findById(sno);
    }

    public List<Territory> findActiveIntersectingBbox(BigDecimal minLat, BigDecimal minLng,
                                                     BigDecimal maxLat, BigDecimal maxLng, int limit) {
        return territoryJpaRepository.findActiveIntersectingBbox(
                minLat, minLng, maxLat, maxLng, PageRequest.of(0, limit));
    }

    public List<Territory> findAllByIdForUpdate(List<Long> snos) {
        return territoryJpaRepository.findAllByIdForUpdate(snos);
    }

    public List<TerritoryOwnerArea> findTopOwnersByArea(int limit) {
        return territoryJpaRepository.findTopOwnersByArea(limit).stream()
                .map(p -> new TerritoryOwnerArea(
                        p.getOwnerMemberSno(),
                        p.getTotalAreaSqm() == null ? 0.0 : p.getTotalAreaSqm(),
                        p.getTerritoryCount() == null ? 0L : p.getTerritoryCount(),
                        p.getTotalHexCount() == null ? 0L : p.getTotalHexCount()))
                .toList();
    }

    public Page<TerritoryOwnerArea> findOwnersByAreaPaged(Pageable pageable) {
        return territoryJpaRepository.findOwnersByAreaPaged(pageable)
                .map(p -> new TerritoryOwnerArea(
                        p.getOwnerMemberSno(),
                        p.getTotalAreaSqm() == null ? 0.0 : p.getTotalAreaSqm(),
                        p.getTerritoryCount() == null ? 0L : p.getTerritoryCount(),
                        p.getTotalHexCount() == null ? 0L : p.getTotalHexCount()));
    }

    public Page<Territory> findAllActiveForAdmin(Pageable pageable) {
        return territoryJpaRepository.findByStatusOrderByCreateAtDesc(Territory.STATUS_ACTIVE, pageable);
    }

    public List<Territory> findActiveMissingHex() {
        return territoryJpaRepository.findActiveMissingHex();
    }

    public TerritoryCaptureCount countCapturesBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        TerritoryCaptureCountProjection p = territoryJpaRepository.countCapturesBetween(fromInclusive, toExclusive);
        long captured = p == null || p.getCapturedTerritories() == null ? 0L : p.getCapturedTerritories();
        long owners = p == null || p.getDistinctOwners() == null ? 0L : p.getDistinctOwners();
        return new TerritoryCaptureCount(captured, owners);
    }

    public void deleteAll() {
        territoryJpaRepository.deleteAllInBatch();
    }

    @Transactional
    public void deleteAllByOwnerMemberSno(Long memberSno) { territoryJpaRepository.deleteAllByOwnerMemberSno(memberSno); }
}

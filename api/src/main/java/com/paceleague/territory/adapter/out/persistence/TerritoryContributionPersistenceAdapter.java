package com.paceleague.territory.adapter.out.persistence;

import com.paceleague.territory.application.port.out.TerritoryContributionRepositoryPort;
import com.paceleague.territory.domain.entity.TerritoryContribution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TerritoryContributionPersistenceAdapter implements TerritoryContributionRepositoryPort {

    private final TerritoryContributionJpaRepository territoryContributionJpaRepository;

    public TerritoryContribution save(TerritoryContribution contribution) {
        return territoryContributionJpaRepository.save(contribution);
    }

    public List<TerritoryContribution> findByTerritorySnoAndCreatedAfter(Long territorySno, LocalDateTime after) {
        return territoryContributionJpaRepository.findByTerritorySnoAndCreateAtAfter(territorySno, after);
    }

    public void deleteByTerritorySno(Long territorySno) {
        territoryContributionJpaRepository.deleteByTerritorySno(territorySno);
    }
}

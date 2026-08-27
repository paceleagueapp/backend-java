package com.example.paceleague.territory.application.port.out;

import com.example.paceleague.territory.domain.entity.TerritoryContribution;

import java.time.LocalDateTime;
import java.util.List;

public interface TerritoryContributionRepositoryPort {

    TerritoryContribution save(TerritoryContribution contribution);

    List<TerritoryContribution> findByTerritorySnoAndCreatedAfter(Long territorySno, LocalDateTime after);

    void deleteByTerritorySno(Long territorySno);
}

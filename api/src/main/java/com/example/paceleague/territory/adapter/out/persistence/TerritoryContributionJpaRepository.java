package com.example.paceleague.territory.adapter.out.persistence;

import com.example.paceleague.territory.domain.entity.TerritoryContribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TerritoryContributionJpaRepository extends JpaRepository<TerritoryContribution, Long> {

    List<TerritoryContribution> findByTerritorySnoAndCreateAtAfter(Long territorySno, LocalDateTime after);

    void deleteByTerritorySno(Long territorySno);
}

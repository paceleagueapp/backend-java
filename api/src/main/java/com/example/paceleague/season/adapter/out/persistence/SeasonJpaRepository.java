package com.example.paceleague.season.adapter.out.persistence;

import com.example.paceleague.season.domain.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonJpaRepository extends JpaRepository<Season, Long> {
    Season findTopByOrderByStartDtDesc();
}

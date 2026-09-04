package com.paceleague.season.adapter.out.persistence;

import com.paceleague.season.domain.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonJpaRepository extends JpaRepository<Season, Long> {
    Season findTopByOrderByStartDtDesc();
}

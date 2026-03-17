package com.example.paceleague.season.repository;

import com.example.paceleague.season.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Season findTopByOrderByStartDtDesc();
}

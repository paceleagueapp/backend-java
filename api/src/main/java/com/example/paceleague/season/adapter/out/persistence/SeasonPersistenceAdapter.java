package com.example.paceleague.season.adapter.out.persistence;

import com.example.paceleague.season.application.port.out.SeasonRepositoryPort;
import com.example.paceleague.season.domain.entity.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeasonPersistenceAdapter implements SeasonRepositoryPort {

    private final SeasonJpaRepository seasonJpaRepository;

    public Season findLatestSeason() {
        return seasonJpaRepository.findTopByOrderByStartDtDesc();
    }
}

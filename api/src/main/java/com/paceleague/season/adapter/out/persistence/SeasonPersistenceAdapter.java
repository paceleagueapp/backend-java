package com.paceleague.season.adapter.out.persistence;

import com.paceleague.season.application.port.out.SeasonRepositoryPort;
import com.paceleague.season.domain.entity.Season;
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

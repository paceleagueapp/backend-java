package com.example.paceleague.season.application.service;

import com.example.paceleague.season.application.port.in.GetCurrentSeasonPort;
import com.example.paceleague.season.application.port.out.SeasonRepositoryPort;
import com.example.paceleague.season.domain.entity.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonQueryService implements GetCurrentSeasonPort {

    private final SeasonRepositoryPort seasonRepositoryPort;

    public Season getCurrentSeason() {
        return seasonRepositoryPort.findLatestSeason();
    }
}

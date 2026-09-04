package com.paceleague.season.application.service;

import com.paceleague.season.application.port.in.shared.GetCurrentSeasonPort;
import com.paceleague.season.application.port.out.SeasonRepositoryPort;
import com.paceleague.season.domain.entity.Season;
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

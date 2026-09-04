package com.paceleague.season.application.port.out;

import com.paceleague.season.domain.entity.Season;

public interface SeasonRepositoryPort {
    Season findLatestSeason();
}

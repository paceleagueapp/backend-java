package com.example.paceleague.season.application.port.out;

import com.example.paceleague.season.domain.entity.Season;

public interface SeasonRepositoryPort {
    Season findLatestSeason();
}

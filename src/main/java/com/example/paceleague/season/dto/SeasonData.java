package com.example.paceleague.season.dto;

import java.time.Instant;

public record SeasonData(
        long sno,
        long season,
        Instant startDt,
        Instant endDt
){}

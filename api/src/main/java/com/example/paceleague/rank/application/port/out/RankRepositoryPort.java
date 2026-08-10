package com.example.paceleague.rank.application.port.out;

import com.example.paceleague.rank.domain.entity.Rank;

public interface RankRepositoryPort {
    Rank save(Rank rank);
}

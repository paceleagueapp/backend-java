package com.paceleague.rank.application.port.out;

import com.paceleague.rank.domain.entity.Rank;

public interface RankRepositoryPort {
    Rank save(Rank rank);
}

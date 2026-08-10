package com.example.paceleague.rank.adapter.out.persistence;

import com.example.paceleague.rank.application.port.out.RankRepositoryPort;
import com.example.paceleague.rank.domain.entity.Rank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankPersistenceAdapter implements RankRepositoryPort {

    private final RankJpaRepository rankJpaRepository;

    public Rank save(Rank rank) {
        return rankJpaRepository.save(rank);
    }
}

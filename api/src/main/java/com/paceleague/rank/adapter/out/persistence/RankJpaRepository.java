package com.paceleague.rank.adapter.out.persistence;

import com.paceleague.rank.domain.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankJpaRepository extends JpaRepository<Rank, Long> {
}

package com.example.paceleague.rank.adapter.out.persistence;

import com.example.paceleague.rank.domain.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankJpaRepository extends JpaRepository<Rank, Long> {
}

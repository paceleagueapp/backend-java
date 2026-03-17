package com.example.paceleague.rank.repository;

import com.example.paceleague.rank.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankRepository extends JpaRepository<Rank, Long> {
}

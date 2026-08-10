package com.example.paceleague.board.adapter.out.persistence;

import com.example.paceleague.board.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardJpaRepository extends JpaRepository<Board, Long> {
    List<Board> findAllByOrderByDisplayOrderAsc();
}

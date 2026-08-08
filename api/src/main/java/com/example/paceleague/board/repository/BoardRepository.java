package com.example.paceleague.board.repository;

import com.example.paceleague.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findAllByOrderByDisplayOrderAsc();
}

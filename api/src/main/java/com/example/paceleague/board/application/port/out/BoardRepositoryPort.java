package com.example.paceleague.board.application.port.out;

import com.example.paceleague.board.domain.entity.Board;

import java.util.List;
import java.util.Optional;

public interface BoardRepositoryPort {
    Optional<Board> findById(Long sno);

    List<Board> findAllByOrderByDisplayOrderAsc();
}

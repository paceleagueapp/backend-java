package com.example.paceleague.board.adapter.out.persistence;

import com.example.paceleague.board.application.port.out.BoardRepositoryPort;
import com.example.paceleague.board.domain.entity.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BoardPersistenceAdapter implements BoardRepositoryPort {

    private final BoardJpaRepository boardJpaRepository;

    public Optional<Board> findById(Long sno) {
        return boardJpaRepository.findById(sno);
    }

    public List<Board> findAllByOrderByDisplayOrderAsc() {
        return boardJpaRepository.findAllByOrderByDisplayOrderAsc();
    }
}

package com.example.paceleague.board.application.dto;

import com.example.paceleague.board.domain.entity.Board;

public record BoardResponse(
        Long sno,
        String slug,
        String name,
        String description
) {
    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getSno(),
                board.getSlug(),
                board.getName(),
                board.getDescription()
        );
    }
}

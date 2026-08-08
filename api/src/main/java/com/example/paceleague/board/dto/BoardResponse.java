package com.example.paceleague.board.dto;

import com.example.paceleague.board.entity.Board;

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

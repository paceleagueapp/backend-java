package com.example.paceleague.board.application.dto;

import com.example.paceleague.board.domain.entity.Board;
import com.example.paceleague.board.domain.policy.BoardLabelPolicy;
import com.example.paceleague.common.i18n.Language;

public record BoardResponse(
        Long sno,
        String slug,
        String name,
        String description
) {
    public static BoardResponse from(Board board, Language lang) {
        return new BoardResponse(
                board.getSno(),
                board.getSlug(),
                BoardLabelPolicy.name(board.getSlug(), lang, board.getName()),
                BoardLabelPolicy.description(board.getSlug(), lang, board.getDescription())
        );
    }
}

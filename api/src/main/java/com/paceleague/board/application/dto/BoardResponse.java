package com.paceleague.board.application.dto;

import com.paceleague.board.domain.entity.Board;
import com.paceleague.board.domain.policy.BoardLabelPolicy;
import com.paceleague.common.i18n.Language;

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

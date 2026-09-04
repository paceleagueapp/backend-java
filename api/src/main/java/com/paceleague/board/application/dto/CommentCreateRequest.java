package com.paceleague.board.application.dto;

public record CommentCreateRequest(
        String content,
        Long parentCommentSno
) {
}

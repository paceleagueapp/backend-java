package com.example.paceleague.board.dto;

public record CommentCreateRequest(
        String content,
        Long parentCommentSno
) {
}

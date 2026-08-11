package com.example.paceleague.board.application.dto;

public record PostCreateRequest(
        String title,
        String content,
        Long recordSno
) {
}

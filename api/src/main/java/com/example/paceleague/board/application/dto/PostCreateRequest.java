package com.example.paceleague.board.application.dto;

import java.util.List;

public record PostCreateRequest(
        String title,
        String content,
        Long recordSno,
        List<Long> attachmentMediaIds
) {
}

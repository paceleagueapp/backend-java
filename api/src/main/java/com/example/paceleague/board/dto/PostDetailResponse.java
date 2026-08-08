package com.example.paceleague.board.dto;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long sno,
        Long boardSno,
        String boardName,
        String title,
        String content,
        Long memberSno,
        String nickname,
        int viewCount,
        int score,
        Integer myVote,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}

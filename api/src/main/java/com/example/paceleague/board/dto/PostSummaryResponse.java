package com.example.paceleague.board.dto;

import com.example.paceleague.board.entity.Post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long sno,
        String title,
        String nickname,
        int viewCount,
        int score,
        long commentCount,
        LocalDateTime createAt
) {
    public static PostSummaryResponse from(Post post, String nickname, long commentCount) {
        return new PostSummaryResponse(
                post.getSno(),
                post.getTitle(),
                nickname,
                post.getViewCount(),
                post.getScore(),
                commentCount,
                post.getCreateAt()
        );
    }
}

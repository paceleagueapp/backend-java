package com.example.paceleague.board.application.dto;

import com.example.paceleague.board.domain.entity.Post;
import com.example.paceleague.rank.domain.enums.RankTier;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long sno,
        String title,
        String nickname,
        RankTier authorTier,
        Long recordSno,
        int viewCount,
        int score,
        long commentCount,
        LocalDateTime createAt
) {
    public static PostSummaryResponse from(Post post, String nickname, RankTier authorTier, long commentCount) {
        return new PostSummaryResponse(
                post.getSno(),
                post.getTitle(),
                nickname,
                authorTier,
                post.getRecordSno(),
                post.getViewCount(),
                post.getScore(),
                commentCount,
                post.getCreateAt()
        );
    }
}

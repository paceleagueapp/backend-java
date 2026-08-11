package com.example.paceleague.board.application.dto;

import com.example.paceleague.board.domain.entity.Post;
import com.example.paceleague.common.i18n.Language;
import com.example.paceleague.rank.domain.enums.RankTier;
import com.example.paceleague.rank.domain.policy.RankTierLabelPolicy;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long sno,
        String title,
        String nickname,
        RankTier authorTier,
        String authorTierLabel,
        Long recordSno,
        long attachmentCount,
        int viewCount,
        int score,
        long commentCount,
        LocalDateTime createAt
) {
    public static PostSummaryResponse from(
            Post post, String nickname, RankTier authorTier, Language lang, long attachmentCount, long commentCount
    ) {
        return new PostSummaryResponse(
                post.getSno(),
                post.getTitle(),
                nickname,
                authorTier,
                RankTierLabelPolicy.label(authorTier, lang),
                post.getRecordSno(),
                attachmentCount,
                post.getViewCount(),
                post.getScore(),
                commentCount,
                post.getCreateAt()
        );
    }
}

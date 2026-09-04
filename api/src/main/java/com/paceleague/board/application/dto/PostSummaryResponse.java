package com.paceleague.board.application.dto;

import com.paceleague.board.domain.entity.Post;
import com.paceleague.board.domain.policy.PostContentSanitizer;
import com.paceleague.common.i18n.Language;
import com.paceleague.crew.application.port.in.GetMemberCrewBadgePort.CrewBadge;
import com.paceleague.rank.domain.enums.RankTier;
import com.paceleague.rank.domain.policy.RankTierLabelPolicy;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long sno,
        String title,
        String nickname,
        RankTier authorTier,
        String authorTierLabel,
        String authorCrewName,
        String authorCrewIconUrl,
        Long recordSno,
        String contentSnippet,
        String thumbnailUrl,
        String thumbnailType,
        int viewCount,
        int score,
        long commentCount,
        LocalDateTime createAt
) {
    public static PostSummaryResponse from(
            Post post, String nickname, RankTier authorTier, CrewBadge authorCrew, Language lang, long commentCount
    ) {
        // 이미지/동영상은 이제 본문 HTML 안에 인라인으로 들어있으므로(media 테이블의 postSno 연결에 의존하지 않고)
        // 본문에서 첫 미디어를 직접 찾아 목록용 썸네일로 내려준다.
        PostContentSanitizer.MediaPreview preview = PostContentSanitizer.firstMediaPreview(post.getContent());

        return new PostSummaryResponse(
                post.getSno(),
                post.getTitle(),
                nickname,
                authorTier,
                RankTierLabelPolicy.label(authorTier, lang),
                authorCrew == null ? null : authorCrew.crewName(),
                authorCrew == null ? null : authorCrew.crewIconUrl(),
                post.getRecordSno(),
                PostContentSanitizer.snippet(post.getContent()),
                preview == null ? null : preview.url(),
                preview == null ? null : preview.type(),
                post.getViewCount(),
                post.getScore(),
                commentCount,
                post.getCreateAt()
        );
    }
}

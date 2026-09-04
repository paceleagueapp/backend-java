package com.paceleague.board.application.dto;

import com.paceleague.media.application.dto.MediaAttachmentResponse;
import com.paceleague.rank.domain.enums.RankTier;
import com.paceleague.record.application.dto.RunningRecordResponse;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long sno,
        Long boardSno,
        String boardName,
        String title,
        String content,
        Long memberSno,
        String nickname,
        RankTier authorTier,
        String authorTierLabel,
        String authorCrewName,
        String authorCrewIconUrl,
        RunningRecordResponse attachedRecord,
        List<MediaAttachmentResponse> attachments,
        int viewCount,
        int score,
        Integer myVote,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}

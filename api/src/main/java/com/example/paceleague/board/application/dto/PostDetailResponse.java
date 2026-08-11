package com.example.paceleague.board.application.dto;

import com.example.paceleague.media.application.dto.MediaAttachmentResponse;
import com.example.paceleague.rank.domain.enums.RankTier;
import com.example.paceleague.record.application.dto.RunningRecordResponse;

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
        RunningRecordResponse attachedRecord,
        List<MediaAttachmentResponse> attachments,
        int viewCount,
        int score,
        Integer myVote,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}

package com.example.paceleague.board.application.dto;

import com.example.paceleague.rank.domain.enums.RankTier;
import com.example.paceleague.record.application.dto.RunningRecordResponse;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long sno,
        Long boardSno,
        String boardName,
        String title,
        String content,
        Long memberSno,
        String nickname,
        RankTier authorTier,
        RunningRecordResponse attachedRecord,
        int viewCount,
        int score,
        Integer myVote,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}

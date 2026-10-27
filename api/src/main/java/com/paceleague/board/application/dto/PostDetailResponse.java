package com.paceleague.board.application.dto;

import com.paceleague.media.application.dto.MediaAttachmentResponse;
import com.paceleague.rank.domain.enums.RankTier;
import com.paceleague.record.application.dto.RunningRecordResponse;

import com.paceleague.record.application.dto.CoursePoint;

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
        // 코스추천 게시판 글에 첨부된 러닝의 경로(위경도). 다른 게시판이거나 첨부 러닝에 GPS 가 없으면 빈 목록.
        List<CoursePoint> course,
        List<MediaAttachmentResponse> attachments,
        int viewCount,
        int score,
        Integer myVote,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}

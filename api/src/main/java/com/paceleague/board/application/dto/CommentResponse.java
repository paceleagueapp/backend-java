package com.paceleague.board.application.dto;

import com.paceleague.board.domain.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long sno,
        Long memberSno,
        String nickname,
        String content,
        int score,
        Integer myVote,
        LocalDateTime createAt,
        List<CommentResponse> replies
) {
    public static CommentResponse from(Comment comment, String nickname, Integer myVote, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getSno(),
                comment.getMemberSno(),
                nickname,
                comment.getContent(),
                comment.getScore(),
                myVote,
                comment.getCreateAt(),
                replies
        );
    }
}

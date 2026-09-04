package com.paceleague.board.application.dto;

public record VoteResponse(
        int score,
        Integer myVote
) {
}

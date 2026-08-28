package com.example.paceleague.crew.application.dto;

import java.time.LocalDateTime;

// GET /api/crew/{sno}/join-requests — 크루장이 보는 가입신청 한 건.
public record CrewJoinRequestResponse(
        Long id,
        Long memberSno,
        String nickname,
        String message,
        String status,
        LocalDateTime createAt
) {
}

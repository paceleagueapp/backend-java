package com.paceleague.crew.application.dto;

import java.time.LocalDateTime;

// GET /api/crew/invitations/me — 내가 받은 초대 한 건.
public record CrewInvitationResponse(
        Long id,
        Long crewSno,
        String crewName,
        String crewIconUrl,
        String inviterNickname,
        String status,
        LocalDateTime createAt,
        LocalDateTime expiresAt
) {
}

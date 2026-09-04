package com.paceleague.crew.application.dto;

import com.paceleague.rank.domain.enums.RankTier;

import java.time.LocalDateTime;

public record CrewMemberResponse(
        Long memberSno,
        String nickname,
        String role,
        RankTier tier,
        String tierLabel,
        LocalDateTime joinedAt
) {
}

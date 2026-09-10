package com.paceleague.member.application.dto;

import java.time.Instant;

public record AdminMemberSummary(
        Long sno,
        String memberId,
        String nickname,
        String email,
        String status,
        Instant createdAt
) {}

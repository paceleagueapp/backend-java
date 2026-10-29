package com.paceleague.member.application.dto;

import java.time.Instant;

public record AdminMemberSummary(
        Long sno,
        String memberId,
        String nickname,
        String email,
        String status,
        Instant createdAt,
        // FCM 기기 토큰(member_device_token)이 1개라도 등록돼 있는지.
        boolean hasFcmToken
) {}

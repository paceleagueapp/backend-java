package com.paceleague.member.application.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record AdminMemberDetail(
        Long sno,
        String memberId,
        String nickname,
        String email,
        String status,
        Instant createdAt,
        LocalDateTime withdrawnAt,
        // 푸시(서비스) 알림 동의 여부 — member_agreement의 PUSH_SERVICE 행 기준.
        boolean pushNotificationAgreed,
        // 아직 한 번도 응답한 적 없으면(로그인 동의 화면을 거치지 않은 경우) false.
        boolean pushNotificationAnswered
) {}

package com.paceleague.notification.application.dto;

import java.time.LocalDateTime;
import java.util.List;

// 관리자 알림 상세 — 본문 HTML + 받는 회원별 발송 여부.
public record AdminNotificationDetail(
        Long sno,
        String title,
        String content,
        int targetCount,
        int sentCount,
        List<Target> targets,
        LocalDateTime createdAt
) {
    // sent=false: 알림 거부/탈퇴/등록된 FCM 토큰 없음/발송 실패
    public record Target(Long memberSno, String nickname, boolean sent) {}
}

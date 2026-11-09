package com.paceleague.notification.application.dto;

import com.paceleague.notification.domain.entity.AdminNotification;

import java.time.LocalDateTime;

// 앱 알림 상세(GET /api/notification/notice/{sno}) — 관리자가 보낸 알림의 제목 + 본문 HTML.
public record NoticeResponse(
        Long sno,
        String title,
        String content,
        LocalDateTime createdAt
) {
    public static NoticeResponse from(AdminNotification n) {
        return new NoticeResponse(n.getSno(), n.getTitle(), n.getContent(), n.getCreatedAt());
    }
}

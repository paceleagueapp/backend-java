package com.paceleague.notification.application.dto;

import com.paceleague.notification.domain.entity.NotificationHistory;

import java.time.LocalDateTime;

public record NotificationHistoryItem(
        Long sno,
        String kind,
        String title,
        String body,
        String deeplink,
        LocalDateTime createdAt
) {
    public static NotificationHistoryItem from(NotificationHistory n) {
        return new NotificationHistoryItem(n.getSno(), n.getKind(), n.getTitle(), n.getBody(), n.getDeeplink(), n.getCreatedAt());
    }
}

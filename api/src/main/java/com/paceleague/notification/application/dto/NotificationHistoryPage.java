package com.paceleague.notification.application.dto;

import java.util.List;

// 더보기(커서) 페이지. hasMore 면 nextCursor 를 다음 요청의 cursor 로 넘긴다.
public record NotificationHistoryPage(
        List<NotificationHistoryItem> items,
        boolean hasMore,
        Long nextCursor
) {
}

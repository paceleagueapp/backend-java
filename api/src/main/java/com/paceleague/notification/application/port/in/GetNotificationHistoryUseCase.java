package com.paceleague.notification.application.port.in;

import com.paceleague.notification.application.dto.NotificationHistoryPage;

public interface GetNotificationHistoryUseCase {

    // cursor == null 이면 최신부터. size 는 1~50 으로 보정.
    NotificationHistoryPage getHistory(Long memberSno, Long cursor, int size);
}

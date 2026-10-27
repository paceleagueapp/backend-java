package com.paceleague.notification.application.port.out;

import com.paceleague.notification.domain.entity.NotificationHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationHistoryRepositoryPort {

    NotificationHistory save(NotificationHistory history);

    // 회원에게 보이는 알림(본인 개인 알림 + since 이후 전체 발송분)을 최신순으로 limit 건.
    // beforeSno 가 있으면 그보다 오래된 것만(더보기 커서).
    List<NotificationHistory> findVisibleTo(Long memberSno, LocalDateTime since, Long beforeSno, int limit);
}

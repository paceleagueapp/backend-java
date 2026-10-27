package com.paceleague.notification.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberJoinedAtPort;
import com.paceleague.notification.application.dto.NotificationHistoryItem;
import com.paceleague.notification.application.dto.NotificationHistoryPage;
import com.paceleague.notification.application.port.in.GetNotificationHistoryUseCase;
import com.paceleague.notification.application.port.out.NotificationHistoryRepositoryPort;
import com.paceleague.notification.domain.entity.NotificationHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationHistoryQueryService implements GetNotificationHistoryUseCase {

    static final int MAX_SIZE = 50;

    private final NotificationHistoryRepositoryPort notificationHistoryRepositoryPort;
    private final GetMemberJoinedAtPort getMemberJoinedAtPort;

    @Override
    public NotificationHistoryPage getHistory(Long memberSno, Long cursor, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);

        // 전체 발송분은 가입 이후 것만. created_at 은 LocalDateTime.now()(서버 시스템 TZ)로 저장되므로
        // 가입 시각(Instant)도 같은 TZ 로 바꿔 비교한다.
        LocalDateTime since = getMemberJoinedAtPort.getJoinedAt(memberSno)
                .map(joinedAt -> LocalDateTime.ofInstant(joinedAt, ZoneId.systemDefault()))
                .orElse(null);
        if (since == null) {
            return new NotificationHistoryPage(List.of(), false, null);
        }

        // size+1 건을 읽어 다음 페이지 존재 여부를 판단한다.
        List<NotificationHistory> rows =
                notificationHistoryRepositoryPort.findVisibleTo(memberSno, since, cursor, safeSize + 1);
        boolean hasMore = rows.size() > safeSize;
        List<NotificationHistoryItem> items = rows.stream()
                .limit(safeSize)
                .map(NotificationHistoryItem::from)
                .toList();
        Long nextCursor = hasMore ? items.get(items.size() - 1).sno() : null;
        return new NotificationHistoryPage(items, hasMore, nextCursor);
    }
}

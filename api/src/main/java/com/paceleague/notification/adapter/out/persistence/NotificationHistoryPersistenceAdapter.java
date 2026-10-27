package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.application.port.out.NotificationHistoryRepositoryPort;
import com.paceleague.notification.domain.entity.NotificationHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationHistoryPersistenceAdapter implements NotificationHistoryRepositoryPort {

    private final NotificationHistoryJpaRepository jpaRepository;

    @Override
    @Transactional
    public NotificationHistory save(NotificationHistory history) {
        return jpaRepository.save(history);
    }

    @Override
    public List<NotificationHistory> findVisibleTo(Long memberSno, LocalDateTime since, Long beforeSno, int limit) {
        return jpaRepository.findVisibleTo(memberSno, since, beforeSno, PageRequest.of(0, limit));
    }
}

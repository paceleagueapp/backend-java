package com.paceleague.notification.application.port.out;

import com.paceleague.notification.domain.entity.AdminNotification;
import com.paceleague.notification.domain.entity.AdminNotificationTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AdminNotificationRepositoryPort {
    AdminNotification save(AdminNotification notification);

    Optional<AdminNotification> findById(Long sno);

    Page<AdminNotification> findAll(Pageable pageable);

    void saveTargets(List<AdminNotificationTarget> targets);

    // 받는 회원 목록, member_sno 순
    List<AdminNotificationTarget> findTargets(Long notificationSno);

    // 목록 화면용 — 여러 알림의 받는 회원을 한 번에
    List<AdminNotificationTarget> findTargetsIn(Collection<Long> notificationSnos);

    boolean isTarget(Long notificationSno, Long memberSno);
}

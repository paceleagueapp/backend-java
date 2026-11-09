package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.application.port.out.AdminNotificationRepositoryPort;
import com.paceleague.notification.domain.entity.AdminNotification;
import com.paceleague.notification.domain.entity.AdminNotificationTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminNotificationPersistenceAdapter implements AdminNotificationRepositoryPort {

    private final AdminNotificationJpaRepository notificationJpaRepository;
    private final AdminNotificationTargetJpaRepository targetJpaRepository;

    @Override
    public AdminNotification save(AdminNotification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public Optional<AdminNotification> findById(Long sno) {
        return notificationJpaRepository.findById(sno);
    }

    @Override
    public Page<AdminNotification> findAll(Pageable pageable) {
        return notificationJpaRepository.findAll(pageable);
    }

    @Override
    public void saveTargets(List<AdminNotificationTarget> targets) {
        targetJpaRepository.saveAll(targets);
    }

    @Override
    public List<AdminNotificationTarget> findTargets(Long notificationSno) {
        return targetJpaRepository.findByNotificationSnoOrderByMemberSno(notificationSno);
    }

    @Override
    public List<AdminNotificationTarget> findTargetsIn(Collection<Long> notificationSnos) {
        if (notificationSnos.isEmpty()) {
            return List.of();
        }
        return targetJpaRepository.findByNotificationSnoInOrderByMemberSno(notificationSnos);
    }

    @Override
    public boolean isTarget(Long notificationSno, Long memberSno) {
        return targetJpaRepository.existsByNotificationSnoAndMemberSno(notificationSno, memberSno);
    }
}

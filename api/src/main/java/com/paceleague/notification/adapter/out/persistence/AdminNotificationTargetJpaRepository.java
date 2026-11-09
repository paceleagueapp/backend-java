package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.domain.entity.AdminNotificationTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AdminNotificationTargetJpaRepository extends JpaRepository<AdminNotificationTarget, Long> {

    List<AdminNotificationTarget> findByNotificationSnoOrderByMemberSno(Long notificationSno);

    List<AdminNotificationTarget> findByNotificationSnoInOrderByMemberSno(Collection<Long> notificationSnos);

    boolean existsByNotificationSnoAndMemberSno(Long notificationSno, Long memberSno);
}

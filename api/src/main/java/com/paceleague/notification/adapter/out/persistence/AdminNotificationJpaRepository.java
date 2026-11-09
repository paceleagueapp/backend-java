package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.domain.entity.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNotificationJpaRepository extends JpaRepository<AdminNotification, Long> {
}

package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.domain.entity.PushSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PushSendLogJpaRepository extends JpaRepository<PushSendLog, Long> {
    Optional<PushSendLog> findByKindAndTargetDate(String kind, LocalDate targetDate);
}

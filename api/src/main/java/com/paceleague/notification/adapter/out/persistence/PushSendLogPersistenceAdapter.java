package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.application.port.out.PushSendLogRepositoryPort;
import com.paceleague.notification.domain.entity.PushSendLog;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PushSendLogPersistenceAdapter implements PushSendLogRepositoryPort {

    private final PushSendLogJpaRepository jpaRepository;

    // 선점은 별도(REQUIRES_NEW) 트랜잭션 — UNIQUE 위반을 잡아 삼켜도 상위 트랜잭션에 영향이 없도록.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<PushSendLog> tryReserve(String kind, LocalDate targetDate) {
        Optional<PushSendLog> existing = jpaRepository.findByKindAndTargetDate(kind, targetDate);
        if (existing.isPresent()) {
            PushSendLog log = existing.get();
            if (!PushSendLog.STATUS_FAILED.equals(log.getStatus())) {
                return Optional.empty(); // 이미 처리됐거나 처리 중
            }
            log.reReserve();
            return Optional.of(jpaRepository.save(log));
        }
        try {
            return Optional.of(jpaRepository.saveAndFlush(PushSendLog.reserve(kind, targetDate)));
        } catch (DataIntegrityViolationException race) {
            return Optional.empty(); // 다른 인스턴스가 방금 선점
        }
    }

    @Override
    @Transactional
    public PushSendLog save(PushSendLog log) {
        return jpaRepository.save(log);
    }
}

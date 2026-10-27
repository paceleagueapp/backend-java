package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.application.port.out.DeviceTokenRepositoryPort;
import com.paceleague.notification.domain.entity.DeviceToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeviceTokenPersistenceAdapter implements DeviceTokenRepositoryPort {

    private final DeviceTokenJpaRepository jpaRepository;

    @Override
    public Optional<DeviceToken> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    @Transactional
    public DeviceToken save(DeviceToken deviceToken) {
        return jpaRepository.save(deviceToken);
    }

    @Override
    public List<DeviceToken> findPageAfter(Long afterSno, int limit) {
        return jpaRepository.findPageAfter(afterSno, PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    public void deleteByTokenAndMemberSno(String token, Long memberSno) {
        jpaRepository.deleteByTokenAndMemberSno(token, memberSno);
    }

    @Override
    @Transactional
    public void deleteAllByTokenIn(Collection<String> tokens) {
        if (tokens.isEmpty()) return;
        jpaRepository.deleteAllByTokenIn(tokens);
    }

    @Override
    @Transactional
    public void deleteAllByMemberSno(Long memberSno) {
        jpaRepository.deleteAllByMemberSno(memberSno);
    }
}

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
    public Optional<DeviceToken> findByFcmId(String fcmId) {
        return jpaRepository.findByFcmId(fcmId);
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
    public List<Long> findMemberSnosIn(Collection<Long> memberSnos) {
        if (memberSnos.isEmpty()) return List.of();
        return jpaRepository.findMemberSnosIn(memberSnos);
    }

    @Override
    public List<DeviceToken> findAllByMemberSno(Long memberSno) {
        return jpaRepository.findAllByMemberSnoOrderByLastSeenAtDesc(memberSno);
    }

    @Override
    @Transactional
    public void deleteByFcmIdAndMemberSno(String fcmId, Long memberSno) {
        jpaRepository.deleteByFcmIdAndMemberSno(fcmId, memberSno);
    }

    @Override
    @Transactional
    public void deleteAllByFcmIdIn(Collection<String> fcmIds) {
        if (fcmIds.isEmpty()) return;
        jpaRepository.deleteAllByFcmIdIn(fcmIds);
    }

    @Override
    @Transactional
    public void deleteAllByMemberSno(Long memberSno) {
        jpaRepository.deleteAllByMemberSno(memberSno);
    }
}

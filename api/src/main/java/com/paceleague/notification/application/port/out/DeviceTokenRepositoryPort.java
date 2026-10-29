package com.paceleague.notification.application.port.out;

import com.paceleague.notification.domain.entity.DeviceToken;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepositoryPort {

    Optional<DeviceToken> findByFcmId(String fcmId);

    DeviceToken save(DeviceToken deviceToken);

    // 전체 회원 발송용 — sno 오름차순으로 afterSno 다음부터 limit 개(첫 페이지는 afterSno=null).
    List<DeviceToken> findPageAfter(Long afterSno, int limit);

    List<Long> findMemberSnosIn(Collection<Long> memberSnos);

    List<DeviceToken> findAllByMemberSno(Long memberSno);

    void deleteByFcmIdAndMemberSno(String fcmId, Long memberSno);

    void deleteAllByFcmIdIn(Collection<String> fcmIds);

    void deleteAllByMemberSno(Long memberSno);
}

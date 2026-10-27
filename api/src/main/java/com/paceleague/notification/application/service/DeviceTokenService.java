package com.paceleague.notification.application.service;

import com.paceleague.notification.application.port.in.RegisterDeviceTokenUseCase;
import com.paceleague.notification.application.port.in.shared.PurgeMemberDeviceTokensPort;
import com.paceleague.notification.application.port.out.DeviceTokenRepositoryPort;
import com.paceleague.notification.domain.entity.DeviceToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeviceTokenService implements RegisterDeviceTokenUseCase, PurgeMemberDeviceTokensPort {

    private static final Set<String> DEVICES = Set.of("ANDROID", "IOS");
    private static final int FCM_ID_MAX_LENGTH = 255;

    private final DeviceTokenRepositoryPort deviceTokenRepositoryPort;

    @Override
    @Transactional
    public boolean register(Long memberSno, String fcmId, String device) {
        String id = fcmId == null ? "" : fcmId.trim();
        String normalizedDevice = device == null ? "" : device.trim().toUpperCase();
        if (id.isEmpty() || id.length() > FCM_ID_MAX_LENGTH || !DEVICES.contains(normalizedDevice)) {
            return false;
        }
        DeviceToken existing = deviceTokenRepositoryPort.findByFcmId(id).orElse(null);
        if (existing == null) {
            deviceTokenRepositoryPort.save(DeviceToken.create(memberSno, id, normalizedDevice));
        } else {
            existing.touch(memberSno, normalizedDevice);
            deviceTokenRepositoryPort.save(existing);
        }
        return true;
    }

    // 본인 토큰만 지운다 — 남의 fcmId 를 알아도 그 기기 수신을 끊을 수 없게.
    @Override
    @Transactional
    public boolean unregister(Long memberSno, String fcmId) {
        String id = fcmId == null ? "" : fcmId.trim();
        if (id.isEmpty()) {
            return false;
        }
        deviceTokenRepositoryPort.deleteByFcmIdAndMemberSno(id, memberSno);
        return true;
    }

    @Override
    @Transactional
    public void purge(Long memberSno) {
        deviceTokenRepositoryPort.deleteAllByMemberSno(memberSno);
    }
}

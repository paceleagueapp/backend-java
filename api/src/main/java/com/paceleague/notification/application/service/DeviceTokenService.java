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

    private static final Set<String> PLATFORMS = Set.of("ANDROID", "IOS");

    private final DeviceTokenRepositoryPort deviceTokenRepositoryPort;

    @Override
    @Transactional
    public void register(Long memberSno, String token, String platform) {
        String normalized = platform == null ? "" : platform.trim().toUpperCase();
        if (!PLATFORMS.contains(normalized)) {
            throw new IllegalArgumentException("platform must be ANDROID or IOS.");
        }
        String trimmed = token.trim();
        DeviceToken existing = deviceTokenRepositoryPort.findByToken(trimmed).orElse(null);
        if (existing == null) {
            deviceTokenRepositoryPort.save(DeviceToken.create(memberSno, trimmed, normalized));
        } else {
            existing.touch(memberSno, normalized);
            deviceTokenRepositoryPort.save(existing);
        }
    }

    // 본인 토큰만 지운다 — 남의 토큰 문자열을 알아도 그 기기 수신을 끊을 수 없게.
    @Override
    @Transactional
    public void unregister(Long memberSno, String token) {
        deviceTokenRepositoryPort.deleteByTokenAndMemberSno(token.trim(), memberSno);
    }

    @Override
    @Transactional
    public void purge(Long memberSno) {
        deviceTokenRepositoryPort.deleteAllByMemberSno(memberSno);
    }
}

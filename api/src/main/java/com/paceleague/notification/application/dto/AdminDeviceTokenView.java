package com.paceleague.notification.application.dto;

import com.paceleague.notification.domain.entity.DeviceToken;

import java.time.LocalDateTime;

// 관리자 회원상세 화면에 보여줄 기기 토큰 1건.
public record AdminDeviceTokenView(
        String device,
        String fcmId,
        LocalDateTime lastSeenAt,
        LocalDateTime createdAt
) {
    public static AdminDeviceTokenView from(DeviceToken d) {
        return new AdminDeviceTokenView(d.getDevice(), d.getFcmId(), d.getLastSeenAt(), d.getCreatedAt());
    }
}

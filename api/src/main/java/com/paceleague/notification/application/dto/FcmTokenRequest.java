package com.paceleague.notification.application.dto;

// fcmId = FCM registration token, device = IOS / ANDROID (대소문자 무관, 해제(DELETE) 때는 생략 가능).
// 값이 잘못돼도 400 대신 data=false 로 돌려주므로 @Valid 제약은 두지 않는다.
public record FcmTokenRequest(
        String fcmId,
        String device
) {}

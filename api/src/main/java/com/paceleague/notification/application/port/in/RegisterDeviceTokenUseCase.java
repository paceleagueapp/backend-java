package com.paceleague.notification.application.port.in;

// 앱이 FCM 토큰(fcmId)을 등록/해제한다. 로그인 직후·토큰 갱신 시 register, 로그아웃 시 unregister.
public interface RegisterDeviceTokenUseCase {

    // 저장(신규) 또는 갱신(이미 있는 fcmId)되면 true, fcmId 가 비었거나 device 가 IOS/ANDROID 가 아니면 false.
    boolean register(Long memberSno, String fcmId, String device);

    boolean unregister(Long memberSno, String fcmId);
}

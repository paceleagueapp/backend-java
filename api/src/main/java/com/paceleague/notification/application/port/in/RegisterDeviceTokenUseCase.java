package com.paceleague.notification.application.port.in;

// 앱이 FCM 토큰을 등록/해제한다. 로그인 직후·토큰 갱신 시 register, 로그아웃 시 unregister.
public interface RegisterDeviceTokenUseCase {

    void register(Long memberSno, String token, String platform);

    void unregister(Long memberSno, String token);
}

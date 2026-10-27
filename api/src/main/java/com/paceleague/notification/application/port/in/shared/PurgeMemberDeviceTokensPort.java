package com.paceleague.notification.application.port.in.shared;

// 회원 탈퇴 시 그 회원의 푸시 기기 토큰을 전부 삭제한다.
public interface PurgeMemberDeviceTokensPort {
    void purge(Long memberSno);
}

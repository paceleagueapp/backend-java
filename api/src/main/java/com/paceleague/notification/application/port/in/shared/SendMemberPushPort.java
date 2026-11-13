package com.paceleague.notification.application.port.in.shared;

import java.util.Map;

// 다른 도메인(coupon 등)이 회원 1명에게 개인 푸시를 보내는 진입 포트.
// 알림내역(notification_history)에는 항상 그 회원의 개인 알림으로 남긴다(알림탭에서 확인 가능).
// 푸시는 탈퇴/서비스 알림 거부 회원이나 등록된 기기 토큰이 없으면 보내지 않는다 — 반환값은 푸시 발송 여부.
// 발송 실패는 예외로 던지지 않고 false — 호출측 업무(쿠폰 지급 등)를 되돌리지 않기 위해.
public interface SendMemberPushPort {
    boolean send(Long memberSno, String kind, String title, String body, String deeplink, Map<String, String> data);
}

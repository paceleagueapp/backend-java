package com.paceleague.notification.application.port.out;

import java.util.Map;

// 푸시 발송 추상화. v1은 전체 토픽 브로드캐스트만 필요.
// 개인 알림(토큰 대상)이 필요해지면 여기에 메서드를 추가한다.
public interface SendPushPort {

    // 전체 회원 토픽으로 알림 1건 발송.
    PushResult sendToAll(String title, String body, Map<String, String> data);

    // delivered=true 면 messageId 가 채워지고, false 면 detail 에 스킵 사유(예: FCM 미설정).
    record PushResult(boolean delivered, String messageId, String detail) {
        public static PushResult sent(String messageId) {
            return new PushResult(true, messageId, null);
        }

        public static PushResult skipped(String detail) {
            return new PushResult(false, null, detail);
        }
    }
}

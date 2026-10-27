package com.paceleague.notification.application.port.out;

import java.util.List;
import java.util.Map;

// 푸시 발송 추상화 — FCM 기기 토큰 묶음으로 보낸다(토픽 미사용: 토픽은 비회원 설치자까지 받기 때문).
public interface SendPushPort {

    // FCM multicast 한 번에 보낼 수 있는 최대 토큰 수.
    int MAX_TOKENS_PER_CALL = 500;

    // tokens 는 MAX_TOKENS_PER_CALL 이하. 발송 호출 자체가 실패하면 예외.
    MulticastResult sendToTokens(List<String> tokens, String title, String body, Map<String, String> data);

    // delivered=false 면 아예 보내지 않음(예: FCM 미설정, detail 에 사유).
    // invalidTokens = FCM 이 더 이상 유효하지 않다고 한 토큰(앱 삭제 등) — 호출측이 지운다.
    record MulticastResult(boolean delivered, int successCount, int failureCount,
                           List<String> invalidTokens, String detail) {
        public static MulticastResult sent(int successCount, int failureCount, List<String> invalidTokens) {
            return new MulticastResult(true, successCount, failureCount, invalidTokens, null);
        }

        public static MulticastResult skipped(String detail) {
            return new MulticastResult(false, 0, 0, List.of(), detail);
        }
    }
}

package com.paceleague.notification.adapter.out.push;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.paceleague.notification.application.port.out.SendPushPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Firebase Admin SDK 로 FCM 기기 토큰 multicast 발송. PushConfig 가 FCM 설정이 있을 때만 이 구현을 SendPushPort 로 등록한다.
public class FcmPushAdapter implements SendPushPort {

    // 이 코드로 실패한 토큰은 다시 보내도 소용없다(앱 삭제/토큰 만료/형식 오류) — 호출측이 삭제한다.
    private static final Set<MessagingErrorCode> INVALID_TOKEN_CODES =
            Set.of(MessagingErrorCode.UNREGISTERED, MessagingErrorCode.INVALID_ARGUMENT);

    private final FirebaseMessaging firebaseMessaging;

    public FcmPushAdapter(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public MulticastResult sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data == null ? Map.of() : data)
                .build();
        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            List<String> invalid = new ArrayList<>();
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse r = responses.get(i);
                if (!r.isSuccessful() && r.getException() != null
                        && INVALID_TOKEN_CODES.contains(r.getException().getMessagingErrorCode())) {
                    invalid.add(tokens.get(i));
                }
            }
            return MulticastResult.sent(response.getSuccessCount(), response.getFailureCount(), invalid);
        } catch (FirebaseMessagingException e) {
            throw new PushSendException(
                    "FCM multicast 발송 실패: " + e.getMessagingErrorCode() + " " + e.getMessage(), e);
        }
    }
}

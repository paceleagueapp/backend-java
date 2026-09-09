package com.paceleague.notification.adapter.out.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.paceleague.notification.application.port.out.SendPushPort;
import com.paceleague.notification.config.FcmProperties;

import java.util.Map;

// Firebase Admin SDK 로 FCM 토픽 발송. PushConfig 가 FCM 설정이 있을 때만 이 구현을 SendPushPort 로 등록한다.
public class FcmPushAdapter implements SendPushPort {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmProperties props;

    public FcmPushAdapter(FirebaseMessaging firebaseMessaging, FcmProperties props) {
        this.firebaseMessaging = firebaseMessaging;
        this.props = props;
    }

    @Override
    public PushResult sendToAll(String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setTopic(props.topic())
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data == null ? Map.of() : data)
                .build();
        try {
            String messageId = firebaseMessaging.send(message);
            return PushResult.sent(messageId);
        } catch (FirebaseMessagingException e) {
            throw new PushSendException(
                    "FCM 토픽(" + props.topic() + ") 발송 실패: " + e.getMessagingErrorCode() + " " + e.getMessage(), e);
        }
    }
}

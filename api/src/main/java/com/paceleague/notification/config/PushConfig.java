package com.paceleague.notification.config;

import com.google.firebase.messaging.FirebaseMessaging;
import com.paceleague.notification.adapter.out.push.FcmPushAdapter;
import com.paceleague.notification.adapter.out.push.NoopPushAdapter;
import com.paceleague.notification.application.port.out.SendPushPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// SendPushPort 를 하나만 등록한다:
//  - FCM 설정(service-account-json) 있으면 FcmPushAdapter (FirebaseMessaging 빈 필요 — FirebaseConfig 가 같은 조건으로 제공)
//  - 없으면 NoopPushAdapter
// @Bean 메서드는 선언 순서대로 처리되고 @ConditionalOnMissingBean 은 그때까지 등록된 빈을 보므로 둘 중 하나만 남는다.
@Configuration
public class PushConfig {

    @Bean
    @ConditionalOnProperty(name = "paceleague.fcm.service-account-json")
    public SendPushPort fcmPushPort(FirebaseMessaging firebaseMessaging, FcmProperties props) {
        return new FcmPushAdapter(firebaseMessaging, props);
    }

    @Bean
    @ConditionalOnMissingBean(SendPushPort.class)
    public SendPushPort noopPushPort() {
        return new NoopPushAdapter();
    }
}

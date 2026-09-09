package com.paceleague.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// paceleague.fcm.service-account-json 이 설정돼 있을 때만 FirebaseApp/FirebaseMessaging 빈을 만든다.
// 미설정(로컬/CI/앱 미배포 단계)에서는 이 빈들이 없고, PushConfig 가 no-op SendPushPort 로 대체한다.
@Configuration
@ConditionalOnProperty(name = "paceleague.fcm.service-account-json")
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp firebaseApp(FcmProperties props) throws IOException {
        if (props.serviceAccountJson() == null || props.serviceAccountJson().isBlank()) {
            throw new IllegalStateException(
                    "paceleague.fcm.service-account-json 이 비어 있습니다. 키를 주입하거나 이 프로퍼티를 아예 설정하지 마세요(미설정이면 발송이 no-op 로 스킵됩니다).");
        }
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        FirebaseOptions.Builder builder = FirebaseOptions.builder();
        try (InputStream credentials = openCredentials(props.serviceAccountJson())) {
            builder.setCredentials(GoogleCredentials.fromStream(credentials));
        }
        if (props.projectId() != null && !props.projectId().isBlank()) {
            builder.setProjectId(props.projectId());
        }
        FirebaseApp app = FirebaseApp.initializeApp(builder.build());
        log.info("FirebaseApp 초기화 완료 (projectId={})", app.getOptions().getProjectId());
        return app;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    // 값이 '{' 로 시작하면 JSON 문자열 그대로, 아니면 로컬 파일 경로로 본다.
    private InputStream openCredentials(String jsonOrPath) throws IOException {
        String v = jsonOrPath.trim();
        if (v.startsWith("{")) {
            return new ByteArrayInputStream(v.getBytes(StandardCharsets.UTF_8));
        }
        return new FileInputStream(v);
    }
}

package com.paceleague.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// paceleague.fcm.* — FCM 발송 설정. TerritoryProperties / JwtProperties 와 같은 방식.
//
//  - serviceAccountJson: Firebase 서비스 계정 키. '{' 로 시작하면 JSON 문자열, 아니면 파일 경로로 해석.
//    운영에서는 env(FCM_SERVICE_ACCOUNT_JSON)로만 주입하고 yml/git 에는 넣지 않는다.
//    값이 비어 있으면 FirebaseConfig 가 비활성화되고 발송은 no-op 로 스킵된다(로컬/미설정 환경 대비).
//  - topic: 전체 회원 브로드캐스트에 쓰는 FCM 토픽 이름. 앱이 이 토픽을 구독한다.
//  - dailyDigest: 매일 아침 "어제 N명이 M개의 땅 점령" 요약 푸시 스케줄 설정.
@ConfigurationProperties(prefix = "paceleague.fcm")
public record FcmProperties(
        String serviceAccountJson,
        String projectId,
        String topic,
        DailyDigest dailyDigest
) {
    public FcmProperties {
        if (topic == null || topic.isBlank()) topic = "all";
        if (dailyDigest == null) dailyDigest = new DailyDigest(null, null, null);
    }

    public record DailyDigest(
            Boolean enabled,
            String cron,
            Boolean sendWhenZero
    ) {
        public DailyDigest {
            if (enabled == null) enabled = false;
            if (cron == null) cron = "0 0 9 * * *"; // 매일 09:00 (스케줄러가 zone="Asia/Seoul" 로 발화)
            if (sendWhenZero == null) sendWhenZero = false; // 어제 점령이 0건이면 기본은 발송 스킵
        }
    }
}

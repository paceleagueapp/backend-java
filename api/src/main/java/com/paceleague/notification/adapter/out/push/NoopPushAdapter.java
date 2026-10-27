package com.paceleague.notification.adapter.out.push;

import com.paceleague.notification.application.port.out.SendPushPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

// FCM 미설정(paceleague.fcm.service-account-json 없음) 환경에서 SendPushPort 자리를 채우는 no-op.
// 실제로 발송하지 않고 delivered=false 를 돌려줘 서비스가 push_send_log 에 SKIPPED 로 남기게 한다.
public class NoopPushAdapter implements SendPushPort {

    private static final Logger log = LoggerFactory.getLogger(NoopPushAdapter.class);

    @Override
    public MulticastResult sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        log.info("FCM 미설정 — 푸시 발송 스킵. tokens={} title=\"{}\" body=\"{}\"", tokens.size(), title, body);
        return MulticastResult.skipped("FCM 미설정(paceleague.fcm.service-account-json 없음)");
    }
}

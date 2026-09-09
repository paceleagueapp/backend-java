package com.paceleague.notification.adapter.in.scheduler;

import com.paceleague.notification.application.port.in.SendDailyTerritoryDigestUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 매일 09:00(KST) "어제 땅따먹기 요약" 전체 푸시.
// 기본 OFF — 앱이 FCM 토픽 구독을 배포한 뒤 paceleague.fcm.daily-digest.enabled=true 로 켠다.
// GpsSessionSweeper 와 마찬가지로 단일 인스턴스 전제(중복 실행은 push_send_log 의 UNIQUE 가드로 1차 차단,
// 스케일아웃 시엔 ShedLock 필요).
@Component
@ConditionalOnProperty(name = "paceleague.fcm.daily-digest.enabled", havingValue = "true")
public class DailyTerritoryDigestScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyTerritoryDigestScheduler.class);

    private final SendDailyTerritoryDigestUseCase useCase;

    public DailyTerritoryDigestScheduler(SendDailyTerritoryDigestUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(cron = "${paceleague.fcm.daily-digest.cron:0 0 9 * * *}", zone = "Asia/Seoul")
    public void run() {
        log.info("일간 땅따먹기 다이제스트 스케줄 실행");
        try {
            useCase.sendForYesterday();
        } catch (Exception e) {
            log.error("일간 땅따먹기 다이제스트 스케줄 실행 실패", e);
        }
    }
}

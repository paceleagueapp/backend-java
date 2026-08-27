package com.example.paceleague.record.adapter.in.scheduler;

import com.example.paceleague.record.application.port.in.SaveGpsSessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

// 앱이 finished=true 청크를 못 보낸 채 끊긴 ACTIVE GPS 세션을 주기적으로 자동 마감한다.
//  - 마지막 청크(update_at) 이후 idle-minutes(기본 30분) 넘게 조용한 ACTIVE 세션을 대상으로
//  - 세션마다 별도 트랜잭션으로 finalizeSession(record 생성 + 점수 산정) 시도
//  - 좌표가 없거나 거리/페이스가 비정상이라 마감이 실패하면 ABANDONED로 표시(다음 스윕에서 다시 잡지 않도록)
// paceleague.gps.sweeper.enabled=false 로 끌 수 있다(예: 다중 인스턴스 전환 시 한 대만 켜기).
@Component
@ConditionalOnProperty(name = "paceleague.gps.sweeper.enabled", havingValue = "true", matchIfMissing = true)
public class GpsSessionSweeper {

    private static final Logger log = LoggerFactory.getLogger(GpsSessionSweeper.class);
    // 한 번의 스윕에서 처리할 최대 세션 수 — 밀린 게 많아도 한 사이클이 과도하게 길어지지 않도록.
    private static final int BATCH_LIMIT = 200;

    private final SaveGpsSessionUseCase saveGpsSessionUseCase;
    private final Duration idleThreshold;

    public GpsSessionSweeper(SaveGpsSessionUseCase saveGpsSessionUseCase,
                             @Value("${paceleague.gps.sweeper.idle-minutes:30}") long idleMinutes) {
        this.saveGpsSessionUseCase = saveGpsSessionUseCase;
        this.idleThreshold = Duration.ofMinutes(idleMinutes);
    }

    @Scheduled(
            fixedDelayString = "${paceleague.gps.sweeper.interval-ms:300000}",
            initialDelayString = "${paceleague.gps.sweeper.initial-delay-ms:120000}"
    )
    public void sweep() {
        List<Long> snos = saveGpsSessionUseCase.findIdleActiveSessionSnos(idleThreshold, BATCH_LIMIT);
        if (snos.isEmpty()) {
            return;
        }

        int finalized = 0;
        int abandoned = 0;
        for (Long sno : snos) {
            try {
                saveGpsSessionUseCase.finalizeSession(sno);
                finalized++;
            } catch (Exception e) {
                log.warn("GPS 세션 sno={} 자동 마감 실패 — ABANDONED 처리: {}", sno, e.getMessage());
                try {
                    saveGpsSessionUseCase.abandonSession(sno);
                    abandoned++;
                } catch (Exception ex) {
                    log.error("GPS 세션 sno={} ABANDONED 처리도 실패", sno, ex);
                }
            }
        }
        log.info("GPS 세션 스위퍼: 대상 {}건 중 {}건 확정, {}건 폐기", snos.size(), finalized, abandoned);
    }
}

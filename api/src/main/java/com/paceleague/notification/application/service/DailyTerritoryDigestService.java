package com.paceleague.notification.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paceleague.notification.application.port.in.SendDailyTerritoryDigestUseCase;
import com.paceleague.notification.application.port.out.PushSendLogRepositoryPort;
import com.paceleague.notification.application.port.out.SendPushPort;
import com.paceleague.notification.config.FcmProperties;
import com.paceleague.notification.domain.entity.PushSendLog;
import com.paceleague.notification.domain.policy.DailyDigestWindow;
import com.paceleague.territory.application.dto.TerritoryCaptureCount;
import com.paceleague.territory.application.port.in.shared.CountTerritoryCapturesPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyTerritoryDigestService implements SendDailyTerritoryDigestUseCase {

    private static final Logger log = LoggerFactory.getLogger(DailyTerritoryDigestService.class);
    static final String KIND = "DAILY_TERRITORY_DIGEST";

    private final CountTerritoryCapturesPort countTerritoryCapturesPort;
    private final SendPushPort sendPushPort;
    private final PushSendLogRepositoryPort pushSendLogPort;
    private final FcmProperties fcmProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendForYesterday() {
        sendForDate(DailyDigestWindow.yesterdayKst());
    }

    // 트랜잭션은 각 포트가 알아서 관리한다(tryReserve = REQUIRES_NEW, count = readOnly, save = 각각).
    void sendForDate(LocalDate targetDate) {
        Optional<PushSendLog> reserved = pushSendLogPort.tryReserve(KIND, targetDate);
        if (reserved.isEmpty()) {
            log.info("일간 땅따먹기 다이제스트: {} 는 이미 처리됨(또는 처리 중) — 스킵", targetDate);
            return;
        }
        PushSendLog logRow = reserved.get();

        DailyDigestWindow.Window window = DailyDigestWindow.forDate(targetDate, ZoneId.systemDefault());
        TerritoryCaptureCount count = countTerritoryCapturesPort.countBetween(
                window.fromInclusive(), window.toExclusive());

        boolean zero = count.capturedTerritories() == 0;
        if (zero && !fcmProperties.dailyDigest().sendWhenZero()) {
            logRow.markSkipped(null, null, "0 captures");
            pushSendLogPort.save(logRow);
            log.info("일간 땅따먹기 다이제스트: {} 점령 0건 — 발송 스킵", targetDate);
            return;
        }

        String title = "땅따먹기 어제 요약";
        String body = zero
                ? "어제는 점령된 땅이 없었어요. 오늘 첫 땅의 주인공이 되어보세요! 🏴"
                : String.format("어제 %d명이 %d개의 땅을 점령했어요 🏴",
                        count.distinctOwners(), count.capturedTerritories());

        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", "daily_territory_digest");
        data.put("date", targetDate.toString());
        data.put("capturedTerritories", String.valueOf(count.capturedTerritories()));
        data.put("distinctOwners", String.valueOf(count.distinctOwners()));
        data.put("deeplink", "paceleague://territory");
        String payloadJson = toJson(data);

        try {
            SendPushPort.PushResult result = sendPushPort.sendToAll(title, body, data);
            if (result.delivered()) {
                logRow.markSent(title, body, payloadJson, result.messageId());
                log.info("일간 땅따먹기 다이제스트: {} 발송 완료 messageId={} 땅={} 유저={}",
                        targetDate, result.messageId(), count.capturedTerritories(), count.distinctOwners());
            } else {
                logRow.markSkipped(title, body, result.detail());
                log.warn("일간 땅따먹기 다이제스트: {} 발송 스킵 — {}", targetDate, result.detail());
            }
        } catch (RuntimeException e) {
            logRow.markFailed(title, body, e.getMessage());
            log.error("일간 땅따먹기 다이제스트: {} 발송 실패", targetDate, e);
        }
        pushSendLogPort.save(logRow);
    }

    private String toJson(Map<String, String> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return null;
        }
    }
}

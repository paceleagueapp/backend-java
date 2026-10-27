package com.paceleague.notification.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paceleague.notification.application.port.in.SendDailyTerritoryDigestUseCase;
import com.paceleague.notification.application.port.out.NotificationHistoryRepositoryPort;
import com.paceleague.notification.application.port.out.PushSendLogRepositoryPort;
import com.paceleague.notification.config.FcmProperties;
import com.paceleague.notification.domain.entity.NotificationHistory;
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
    private static final String DEEPLINK = "paceleague://territory";

    private final CountTerritoryCapturesPort countTerritoryCapturesPort;
    private final MemberBroadcastSender memberBroadcastSender;
    private final PushSendLogRepositoryPort pushSendLogPort;
    private final NotificationHistoryRepositoryPort notificationHistoryPort;
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
        data.put("deeplink", DEEPLINK);
        String payloadJson = toJson(data);

        try {
            MemberBroadcastSender.BroadcastResult result = memberBroadcastSender.sendToAllMembers(title, body, data);
            if (result.delivered()) {
                logRow.markSent(title, body, payloadJson, null, result.detail());
                log.info("일간 땅따먹기 다이제스트: {} 발송 완료 {} 땅={} 유저={}",
                        targetDate, result.detail(), count.capturedTerritories(), count.distinctOwners());
            } else {
                logRow.markSkipped(title, body, result.detail());
                log.warn("일간 땅따먹기 다이제스트: {} 발송 스킵 — {}", targetDate, result.detail());
            }
        } catch (RuntimeException e) {
            logRow.markFailed(title, body, e.getMessage());
            log.error("일간 땅따먹기 다이제스트: {} 발송 실패", targetDate, e);
        }
        pushSendLogPort.save(logRow);

        if (PushSendLog.STATUS_SENT.equals(logRow.getStatus())) {
            saveHistory(title, body);
        }
    }

    // 실제 발송된 것만 알림내역에 남긴다. 저장 실패가 발송 결과(push_send_log)를 되돌리면 안 되므로 로그만.
    private void saveHistory(String title, String body) {
        try {
            notificationHistoryPort.save(NotificationHistory.broadcast(KIND, title, body, DEEPLINK));
        } catch (RuntimeException e) {
            log.error("일간 땅따먹기 다이제스트: 알림내역 저장 실패", e);
        }
    }

    private String toJson(Map<String, String> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return null;
        }
    }
}

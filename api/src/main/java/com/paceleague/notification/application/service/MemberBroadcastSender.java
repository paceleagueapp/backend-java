package com.paceleague.notification.application.service;

import com.paceleague.member.application.port.in.shared.FilterPushReceivableMembersPort;
import com.paceleague.notification.application.port.out.DeviceTokenRepositoryPort;
import com.paceleague.notification.application.port.out.SendPushPort;
import com.paceleague.notification.domain.entity.DeviceToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// "전체 회원" 발송 — 등록된 기기 토큰을 500개씩 훑으며, 탈퇴/서비스 알림 거부 회원의 토큰은 빼고 보낸다.
// 비회원(토큰 미등록 설치자)은 대상이 아니다. FCM 이 무효라고 한 토큰은 그 자리에서 삭제한다.
@Component
@RequiredArgsConstructor
public class MemberBroadcastSender {

    private final DeviceTokenRepositoryPort deviceTokenRepositoryPort;
    private final FilterPushReceivableMembersPort filterPushReceivableMembersPort;
    private final SendPushPort sendPushPort;

    // 한 토큰이라도 성공하면 delivered. 보낼 토큰이 없거나 FCM 미설정이면 미전송(detail 에 사유).
    // 전부 실패했거나 FCM 호출 자체가 실패하면 예외 — 호출측이 FAILED 로 기록한다.
    public BroadcastResult sendToAllMembers(String title, String body, Map<String, String> data) {
        int targets = 0;
        int success = 0;
        int failure = 0;
        int removed = 0;
        Long afterSno = null;

        while (true) {
            List<DeviceToken> page = deviceTokenRepositoryPort.findPageAfter(afterSno, SendPushPort.MAX_TOKENS_PER_CALL);
            if (page.isEmpty()) break;
            afterSno = page.get(page.size() - 1).getSno();

            Set<Long> receivable = filterPushReceivableMembersPort.filterReceivable(
                    page.stream().map(DeviceToken::getMemberSno).collect(Collectors.toSet()));
            List<String> tokens = page.stream()
                    .filter(d -> receivable.contains(d.getMemberSno()))
                    .map(DeviceToken::getToken)
                    .toList();

            if (!tokens.isEmpty()) {
                SendPushPort.MulticastResult result = sendPushPort.sendToTokens(tokens, title, body, data);
                if (!result.delivered()) {
                    return BroadcastResult.skipped(result.detail());
                }
                targets += tokens.size();
                success += result.successCount();
                failure += result.failureCount();
                if (!result.invalidTokens().isEmpty()) {
                    deviceTokenRepositoryPort.deleteAllByTokenIn(result.invalidTokens());
                    removed += result.invalidTokens().size();
                }
            }
            if (page.size() < SendPushPort.MAX_TOKENS_PER_CALL) break;
        }

        if (targets == 0) {
            return BroadcastResult.skipped("no receivable device tokens");
        }
        String summary = String.format("tokens=%d success=%d failure=%d removed=%d", targets, success, failure, removed);
        if (success == 0) {
            throw new IllegalStateException("모든 기기 토큰 발송 실패 — " + summary);
        }
        return BroadcastResult.sent(summary);
    }

    public record BroadcastResult(boolean delivered, String detail) {
        public static BroadcastResult sent(String summary) {
            return new BroadcastResult(true, summary);
        }

        public static BroadcastResult skipped(String detail) {
            return new BroadcastResult(false, detail);
        }
    }
}

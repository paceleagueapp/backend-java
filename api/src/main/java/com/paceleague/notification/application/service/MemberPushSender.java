package com.paceleague.notification.application.service;

import com.paceleague.member.application.port.in.shared.FilterPushReceivableMembersPort;
import com.paceleague.notification.application.port.in.shared.SendMemberPushPort;
import com.paceleague.notification.application.port.out.DeviceTokenRepositoryPort;
import com.paceleague.notification.application.port.out.NotificationHistoryRepositoryPort;
import com.paceleague.notification.application.port.out.SendPushPort;
import com.paceleague.notification.domain.entity.DeviceToken;
import com.paceleague.notification.domain.entity.NotificationHistory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

// 회원 1명 개인 푸시 — MemberBroadcastSender 의 1인 버전. 트랜잭션을 걸지 않아 실패가 호출측 트랜잭션을
// rollback-only 로 만들지 않는다(각 저장/삭제는 어댑터 단위 트랜잭션).
@Service
@RequiredArgsConstructor
public class MemberPushSender implements SendMemberPushPort {

    private static final Logger log = LoggerFactory.getLogger(MemberPushSender.class);

    private final DeviceTokenRepositoryPort deviceTokenRepositoryPort;
    private final FilterPushReceivableMembersPort filterPushReceivableMembersPort;
    private final SendPushPort sendPushPort;
    private final NotificationHistoryRepositoryPort notificationHistoryRepositoryPort;

    @Override
    public boolean send(Long memberSno, String kind, String title, String body, String deeplink, Map<String, String> data) {
        try {
            if (!filterPushReceivableMembersPort.filterReceivable(Set.of(memberSno)).contains(memberSno)) {
                return false;
            }
            List<String> tokens = deviceTokenRepositoryPort.findAllByMemberSno(memberSno).stream()
                    .map(DeviceToken::getFcmId)
                    .limit(SendPushPort.MAX_TOKENS_PER_CALL)
                    .toList();
            if (tokens.isEmpty()) {
                return false;
            }
            SendPushPort.MulticastResult result = sendPushPort.sendToTokens(tokens, title, body, data);
            if (!result.invalidTokens().isEmpty()) {
                deviceTokenRepositoryPort.deleteAllByFcmIdIn(result.invalidTokens());
            }
            if (!result.delivered() || result.successCount() == 0) {
                return false;
            }
            notificationHistoryRepositoryPort.save(NotificationHistory.personal(memberSno, kind, title, body, deeplink));
            return true;
        } catch (RuntimeException e) {
            log.error("개인 푸시 발송 실패 memberSno={} kind={}", memberSno, kind, e);
            return false;
        }
    }
}

package com.paceleague.member.application.port.in.shared;

import java.util.Collection;
import java.util.Set;

// 전체 회원 푸시 발송 대상 거르기 — 주어진 회원 중 탈퇴하지 않았고 서비스 알림(PUSH_SERVICE)을 거부하지 않은 회원만.
// 서비스 알림은 광고가 아니라 사전 동의가 필수는 아니므로, 아직 답하지 않은 회원은 받는 쪽으로 둔다.
public interface FilterPushReceivableMembersPort {
    Set<Long> filterReceivable(Collection<Long> memberSnos);
}

package com.paceleague.notification.application.service;

import com.paceleague.member.application.port.in.shared.FilterPushReceivableMembersPort;
import com.paceleague.notification.application.port.out.DeviceTokenRepositoryPort;
import com.paceleague.notification.application.port.out.SendPushPort;
import com.paceleague.notification.domain.entity.DeviceToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberBroadcastSenderTest {

    @Mock DeviceTokenRepositoryPort deviceTokenRepositoryPort;
    @Mock FilterPushReceivableMembersPort filterPushReceivableMembersPort;
    @Mock SendPushPort sendPushPort;

    MemberBroadcastSender sender;

    @BeforeEach
    void setUp() {
        sender = new MemberBroadcastSender(deviceTokenRepositoryPort, filterPushReceivableMembersPort, sendPushPort);
    }

    private DeviceToken token(long sno, long memberSno, String token) {
        DeviceToken d = DeviceToken.create(memberSno, token, "ANDROID");
        ReflectionTestUtils.setField(d, "sno", sno);
        return d;
    }

    @Test
    void 받을_수_있는_회원의_토큰에만_보낸다() {
        when(deviceTokenRepositoryPort.findPageAfter(isNull(), eq(500)))
                .thenReturn(List.of(token(1, 10, "t-a"), token(2, 20, "t-b"), token(3, 10, "t-c")));
        when(filterPushReceivableMembersPort.filterReceivable(Set.of(10L, 20L))).thenReturn(Set.of(10L)); // 20 = 거부/탈퇴
        when(sendPushPort.sendToTokens(anyList(), anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.MulticastResult.sent(2, 0, List.of()));

        MemberBroadcastSender.BroadcastResult result = sender.sendToAllMembers("t", "b", Map.of());

        verify(sendPushPort).sendToTokens(eq(List.of("t-a", "t-c")), eq("t"), eq("b"), anyMap());
        assertThat(result.delivered()).isTrue();
        assertThat(result.detail()).isEqualTo("tokens=2 success=2 failure=0 removed=0");
    }

    @Test
    void 토큰이_500개를_넘으면_다음_페이지를_이어서_보낸다() {
        List<DeviceToken> first = new ArrayList<>();
        for (int i = 1; i <= 500; i++) first.add(token(i, i, "t" + i));
        when(deviceTokenRepositoryPort.findPageAfter(isNull(), eq(500))).thenReturn(first);
        when(deviceTokenRepositoryPort.findPageAfter(eq(500L), eq(500))).thenReturn(List.of(token(501, 501, "t501")));
        when(filterPushReceivableMembersPort.filterReceivable(any()))
                .thenAnswer(inv -> Set.copyOf((Collection<Long>) inv.getArgument(0)));
        when(sendPushPort.sendToTokens(anyList(), anyString(), anyString(), anyMap()))
                .thenAnswer(inv -> SendPushPort.MulticastResult.sent(((List<?>) inv.getArgument(0)).size(), 0, List.of()));

        MemberBroadcastSender.BroadcastResult result = sender.sendToAllMembers("t", "b", Map.of());

        assertThat(result.detail()).isEqualTo("tokens=501 success=501 failure=0 removed=0");
    }

    @Test
    void FCM이_무효라고_한_토큰은_삭제한다() {
        when(deviceTokenRepositoryPort.findPageAfter(isNull(), eq(500)))
                .thenReturn(List.of(token(1, 10, "ok"), token(2, 10, "dead")));
        when(filterPushReceivableMembersPort.filterReceivable(any())).thenReturn(Set.of(10L));
        when(sendPushPort.sendToTokens(anyList(), anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.MulticastResult.sent(1, 1, List.of("dead")));

        MemberBroadcastSender.BroadcastResult result = sender.sendToAllMembers("t", "b", Map.of());

        verify(deviceTokenRepositoryPort).deleteAllByFcmIdIn(List.of("dead"));
        assertThat(result.detail()).isEqualTo("tokens=2 success=1 failure=1 removed=1");
    }

    @Test
    void 보낼_토큰이_없으면_미전송() {
        when(deviceTokenRepositoryPort.findPageAfter(isNull(), eq(500))).thenReturn(List.of());

        MemberBroadcastSender.BroadcastResult result = sender.sendToAllMembers("t", "b", Map.of());

        assertThat(result.delivered()).isFalse();
        assertThat(result.detail()).isEqualTo("no receivable device tokens");
        verify(sendPushPort, never()).sendToTokens(anyList(), anyString(), anyString(), anyMap());
    }

    @Test
    void FCM_미설정이면_미전송_사유를_그대로_돌려준다() {
        when(deviceTokenRepositoryPort.findPageAfter(isNull(), eq(500))).thenReturn(List.of(token(1, 10, "t")));
        when(filterPushReceivableMembersPort.filterReceivable(any())).thenReturn(Set.of(10L));
        when(sendPushPort.sendToTokens(anyList(), anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.MulticastResult.skipped("FCM 미설정"));

        MemberBroadcastSender.BroadcastResult result = sender.sendToAllMembers("t", "b", Map.of());

        assertThat(result.delivered()).isFalse();
        assertThat(result.detail()).isEqualTo("FCM 미설정");
    }

    @Test
    void 전부_실패하면_예외() {
        when(deviceTokenRepositoryPort.findPageAfter(isNull(), eq(500))).thenReturn(List.of(token(1, 10, "t")));
        when(filterPushReceivableMembersPort.filterReceivable(any())).thenReturn(Set.of(10L));
        when(sendPushPort.sendToTokens(anyList(), anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.MulticastResult.sent(0, 1, List.of()));

        assertThatThrownBy(() -> sender.sendToAllMembers("t", "b", Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}

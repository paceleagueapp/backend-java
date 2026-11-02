package com.paceleague.notification.application.service;

import com.paceleague.member.application.port.in.shared.FilterPushReceivableMembersPort;
import com.paceleague.notification.application.port.out.DeviceTokenRepositoryPort;
import com.paceleague.notification.application.port.out.NotificationHistoryRepositoryPort;
import com.paceleague.notification.application.port.out.SendPushPort;
import com.paceleague.notification.domain.entity.DeviceToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberPushSenderTest {

    @Mock DeviceTokenRepositoryPort deviceTokenRepositoryPort;
    @Mock FilterPushReceivableMembersPort filterPushReceivableMembersPort;
    @Mock SendPushPort sendPushPort;
    @Mock NotificationHistoryRepositoryPort notificationHistoryRepositoryPort;

    MemberPushSender sender;

    @BeforeEach
    void setUp() {
        sender = new MemberPushSender(deviceTokenRepositoryPort, filterPushReceivableMembersPort, sendPushPort,
                notificationHistoryRepositoryPort);
        when(filterPushReceivableMembersPort.filterReceivable(Set.of(7L))).thenReturn(Set.of(7L));
        when(deviceTokenRepositoryPort.findAllByMemberSno(7L)).thenReturn(List.of(DeviceToken.create(7L, "tok", "ANDROID")));
    }

    @Test
    void 발송되면_개인_알림내역을_남긴다() {
        when(sendPushPort.sendToTokens(any(), anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.MulticastResult.sent(1, 0, List.of()));

        assertThat(sender.send(7L, "COUPON", "t", "b", "paceleague://coupons", Map.of())).isTrue();
        verify(notificationHistoryRepositoryPort).save(any());
    }

    @Test
    void 알림을_거부한_회원에게는_보내지_않는다() {
        when(filterPushReceivableMembersPort.filterReceivable(Set.of(7L))).thenReturn(Set.of());

        assertThat(sender.send(7L, "COUPON", "t", "b", null, Map.of())).isFalse();
        verify(sendPushPort, never()).sendToTokens(any(), anyString(), anyString(), anyMap());
    }

    @Test
    void 발송_예외는_삼키고_false() {
        when(sendPushPort.sendToTokens(any(), anyString(), anyString(), anyMap())).thenThrow(new RuntimeException("fcm down"));

        assertThat(sender.send(7L, "COUPON", "t", "b", null, Map.of())).isFalse();
        verify(notificationHistoryRepositoryPort, never()).save(any());
    }
}

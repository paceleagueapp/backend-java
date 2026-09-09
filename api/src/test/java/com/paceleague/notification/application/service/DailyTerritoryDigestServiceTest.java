package com.paceleague.notification.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paceleague.notification.application.port.out.PushSendLogRepositoryPort;
import com.paceleague.notification.application.port.out.SendPushPort;
import com.paceleague.notification.config.FcmProperties;
import com.paceleague.notification.domain.entity.PushSendLog;
import com.paceleague.territory.application.dto.TerritoryCaptureCount;
import com.paceleague.territory.application.port.in.shared.CountTerritoryCapturesPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DailyTerritoryDigestServiceTest {

    @Mock CountTerritoryCapturesPort countTerritoryCapturesPort;
    @Mock SendPushPort sendPushPort;
    @Mock PushSendLogRepositoryPort pushSendLogPort;

    DailyTerritoryDigestService service;

    private final LocalDate targetDate = LocalDate.of(2026, 9, 8);
    private PushSendLog reservedLog;

    @BeforeEach
    void setUp() {
        FcmProperties props = new FcmProperties(null, null, null, null); // sendWhenZero=false, topic=all
        service = new DailyTerritoryDigestService(
                countTerritoryCapturesPort, sendPushPort, pushSendLogPort, props, new ObjectMapper());

        reservedLog = PushSendLog.reserve(DailyTerritoryDigestService.KIND, targetDate);
        when(pushSendLogPort.tryReserve(eq(DailyTerritoryDigestService.KIND), eq(targetDate)))
                .thenReturn(Optional.of(reservedLog));
    }

    @Test
    void 점령이_있으면_유저수_땅수를_문구로_만들어_토픽_발송하고_SENT로_기록한다() {
        when(countTerritoryCapturesPort.countBetween(any(), any()))
                .thenReturn(new TerritoryCaptureCount(128, 42));
        when(sendPushPort.sendToAll(anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.PushResult.sent("projects/x/messages/abc"));

        service.sendForDate(targetDate);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(sendPushPort).sendToAll(eq("땅따먹기 어제 요약"), body.capture(), anyMap());
        assertThat(body.getValue()).contains("42명").contains("128개");

        assertThat(reservedLog.getStatus()).isEqualTo(PushSendLog.STATUS_SENT);
        assertThat(reservedLog.getFcmMessageId()).isEqualTo("projects/x/messages/abc");
        verify(pushSendLogPort).save(reservedLog);
    }

    @Test
    void 점령_0건이고_sendWhenZero_false면_발송하지_않고_SKIPPED로_기록한다() {
        when(countTerritoryCapturesPort.countBetween(any(), any()))
                .thenReturn(new TerritoryCaptureCount(0, 0));

        service.sendForDate(targetDate);

        verify(sendPushPort, never()).sendToAll(anyString(), anyString(), anyMap());
        assertThat(reservedLog.getStatus()).isEqualTo(PushSendLog.STATUS_SKIPPED);
        assertThat(reservedLog.getDetail()).isEqualTo("0 captures");
    }

    @Test
    void 이미_처리된_날이면_아무것도_하지_않는다() {
        when(pushSendLogPort.tryReserve(anyString(), any())).thenReturn(Optional.empty());

        service.sendForDate(targetDate);

        verify(countTerritoryCapturesPort, never()).countBetween(any(), any());
        verify(sendPushPort, never()).sendToAll(anyString(), anyString(), anyMap());
        verify(pushSendLogPort, never()).save(any());
    }

    @Test
    void 발송이_예외로_실패하면_FAILED로_기록하고_예외를_삼킨다() {
        when(countTerritoryCapturesPort.countBetween(any(), any()))
                .thenReturn(new TerritoryCaptureCount(10, 5));
        when(sendPushPort.sendToAll(anyString(), anyString(), anyMap()))
                .thenThrow(new RuntimeException("FCM down"));

        service.sendForDate(targetDate); // no throw

        assertThat(reservedLog.getStatus()).isEqualTo(PushSendLog.STATUS_FAILED);
        assertThat(reservedLog.getDetail()).contains("FCM down");
        verify(pushSendLogPort).save(reservedLog);
    }

    @Test
    void 어댑터가_미전송_결과를_주면_SKIPPED로_기록한다() {
        when(countTerritoryCapturesPort.countBetween(any(), any()))
                .thenReturn(new TerritoryCaptureCount(10, 5));
        when(sendPushPort.sendToAll(anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.PushResult.skipped("FCM 미설정"));

        service.sendForDate(targetDate);

        assertThat(reservedLog.getStatus()).isEqualTo(PushSendLog.STATUS_SKIPPED);
        assertThat(reservedLog.getDetail()).isEqualTo("FCM 미설정");
    }

    @Test
    void sendWhenZero_true면_0건에도_안내_문구로_발송한다() {
        FcmProperties props = new FcmProperties(null, null, null,
                new FcmProperties.DailyDigest(true, null, true));
        service = new DailyTerritoryDigestService(
                countTerritoryCapturesPort, sendPushPort, pushSendLogPort, props, new ObjectMapper());
        when(pushSendLogPort.tryReserve(anyString(), any())).thenReturn(Optional.of(reservedLog));
        when(countTerritoryCapturesPort.countBetween(any(), any()))
                .thenReturn(new TerritoryCaptureCount(0, 0));
        when(sendPushPort.sendToAll(anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.PushResult.sent("m1"));

        service.sendForDate(targetDate);

        verify(sendPushPort).sendToAll(anyString(), anyString(), anyMap());
        assertThat(reservedLog.getStatus()).isEqualTo(PushSendLog.STATUS_SENT);
    }

    @Test
    void 집계_구간은_KST_전날을_저장TZ로_변환한_값이다() {
        when(countTerritoryCapturesPort.countBetween(any(), any()))
                .thenReturn(new TerritoryCaptureCount(1, 1));
        when(sendPushPort.sendToAll(anyString(), anyString(), anyMap()))
                .thenReturn(SendPushPort.PushResult.sent("m"));

        service.sendForDate(targetDate);

        ArgumentCaptor<LocalDateTime> from = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> to = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(countTerritoryCapturesPort).countBetween(from.capture(), to.capture());
        assertThat(java.time.Duration.between(from.getValue(), to.getValue()).toHours()).isEqualTo(24);
    }
}

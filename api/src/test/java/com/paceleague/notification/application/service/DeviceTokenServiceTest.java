package com.paceleague.notification.application.service;

import com.paceleague.notification.application.port.out.DeviceTokenRepositoryPort;
import com.paceleague.notification.domain.entity.DeviceToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    @Mock DeviceTokenRepositoryPort deviceTokenRepositoryPort;

    DeviceTokenService service;

    @BeforeEach
    void setUp() {
        service = new DeviceTokenService(deviceTokenRepositoryPort);
    }

    @Test
    void 새_fcmId는_저장하고_true() {
        when(deviceTokenRepositoryPort.findByFcmId("fcm")).thenReturn(Optional.empty());

        boolean result = service.register(7L, " fcm ", "android");

        assertThat(result).isTrue();
        ArgumentCaptor<DeviceToken> saved = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getMemberSno()).isEqualTo(7L);
        assertThat(saved.getValue().getFcmId()).isEqualTo("fcm");
        assertThat(saved.getValue().getDevice()).isEqualTo("ANDROID");
    }

    @Test
    void 이미_있는_fcmId면_회원과_기기를_갱신하고_true() {
        DeviceToken existing = DeviceToken.create(7L, "fcm", "ANDROID");
        when(deviceTokenRepositoryPort.findByFcmId("fcm")).thenReturn(Optional.of(existing));

        boolean result = service.register(8L, "fcm", "ios");

        assertThat(result).isTrue();
        assertThat(existing.getMemberSno()).isEqualTo(8L);
        assertThat(existing.getDevice()).isEqualTo("IOS");
        verify(deviceTokenRepositoryPort).save(existing);
    }

    @Test
    void device가_IOS_ANDROID가_아니면_저장하지_않고_false() {
        assertThat(service.register(7L, "fcm", "web")).isFalse();
        assertThat(service.register(7L, "fcm", null)).isFalse();
        verify(deviceTokenRepositoryPort, never()).save(any());
    }

    @Test
    void fcmId가_비었으면_저장하지_않고_false() {
        assertThat(service.register(7L, " ", "IOS")).isFalse();
        assertThat(service.register(7L, null, "IOS")).isFalse();
        verify(deviceTokenRepositoryPort, never()).save(any());
    }

    @Test
    void 해제는_본인_토큰만_지우고_true() {
        assertThat(service.unregister(7L, "fcm")).isTrue();

        verify(deviceTokenRepositoryPort).deleteByFcmIdAndMemberSno("fcm", 7L);
    }

    @Test
    void 해제때_fcmId가_없으면_false() {
        assertThat(service.unregister(7L, "")).isFalse();

        verify(deviceTokenRepositoryPort, never()).deleteByFcmIdAndMemberSno(anyString(), anyLong());
    }
}

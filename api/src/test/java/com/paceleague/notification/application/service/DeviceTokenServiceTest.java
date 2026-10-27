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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    void 새_토큰은_새로_저장한다() {
        when(deviceTokenRepositoryPort.findByToken("tok")).thenReturn(Optional.empty());

        service.register(7L, " tok ", "android");

        ArgumentCaptor<DeviceToken> saved = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getMemberSno()).isEqualTo(7L);
        assertThat(saved.getValue().getToken()).isEqualTo("tok");
        assertThat(saved.getValue().getPlatform()).isEqualTo("ANDROID");
    }

    @Test
    void 이미_있는_토큰을_다른_계정이_등록하면_그_계정으로_옮겨진다() {
        DeviceToken existing = DeviceToken.create(7L, "tok", "ANDROID");
        when(deviceTokenRepositoryPort.findByToken("tok")).thenReturn(Optional.of(existing));

        service.register(8L, "tok", "IOS");

        assertThat(existing.getMemberSno()).isEqualTo(8L);
        assertThat(existing.getPlatform()).isEqualTo("IOS");
        verify(deviceTokenRepositoryPort).save(existing);
    }

    @Test
    void 플랫폼이_ANDROID_IOS가_아니면_400() {
        assertThatThrownBy(() -> service.register(7L, "tok", "web"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(deviceTokenRepositoryPort, never()).save(any());
    }

    @Test
    void 해제는_본인_토큰만_지운다() {
        service.unregister(7L, "tok");

        verify(deviceTokenRepositoryPort).deleteByTokenAndMemberSno("tok", 7L);
    }
}

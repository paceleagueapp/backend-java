package com.paceleague.appversion.application.service;

import com.paceleague.appversion.application.dto.AppVersionSummary;
import com.paceleague.appversion.application.port.out.AppVersionPolicyRepositoryPort;
import com.paceleague.appversion.domain.entity.AppVersionPolicy;
import com.paceleague.appversion.domain.enums.AppPlatform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppVersionServiceTest {

    @Mock AppVersionPolicyRepositoryPort appVersionPolicyRepositoryPort;

    AppVersionService service() {
        return new AppVersionService(appVersionPolicyRepositoryPort);
    }

    @Test
    void 처음_설정하는_플랫폼은_새로_생성한다() {
        when(appVersionPolicyRepositoryPort.findByPlatform(AppPlatform.ANDROID)).thenReturn(Optional.empty());
        when(appVersionPolicyRepositoryPort.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(inv -> inv.getArgument(0));

        AppVersionSummary result = service().updateVersion(AppPlatform.ANDROID, "1.2.0", "1.0.0");

        assertThat(result.platform()).isEqualTo(AppPlatform.ANDROID);
        assertThat(result.latestVersion()).isEqualTo("1.2.0");
        assertThat(result.minRequiredVersion()).isEqualTo("1.0.0");
    }

    @Test
    void 기존_정책이_있으면_버전만_갱신한다() {
        AppVersionPolicy existing = AppVersionPolicy.create(AppPlatform.IOS, "1.0.0", "1.0.0");
        when(appVersionPolicyRepositoryPort.findByPlatform(AppPlatform.IOS)).thenReturn(Optional.of(existing));
        when(appVersionPolicyRepositoryPort.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(inv -> inv.getArgument(0));

        service().updateVersion(AppPlatform.IOS, "2.0.0", "1.5.0");

        ArgumentCaptor<AppVersionPolicy> captor = ArgumentCaptor.forClass(AppVersionPolicy.class);
        verify(appVersionPolicyRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getLatestVersion()).isEqualTo("2.0.0");
        assertThat(captor.getValue().getMinRequiredVersion()).isEqualTo("1.5.0");
    }

    @Test
    void 목록조회는_등록된_모든_플랫폼을_요약으로_변환한다() {
        when(appVersionPolicyRepositoryPort.findAll()).thenReturn(List.of(
                AppVersionPolicy.create(AppPlatform.ANDROID, "1.2.0", "1.0.0"),
                AppVersionPolicy.create(AppPlatform.IOS, "1.3.0", "1.1.0")
        ));

        List<AppVersionSummary> result = service().list();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AppVersionSummary::platform)
                .containsExactlyInAnyOrder(AppPlatform.ANDROID, AppPlatform.IOS);
    }
}

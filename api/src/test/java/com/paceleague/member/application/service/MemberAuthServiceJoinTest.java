package com.paceleague.member.application.service;

import com.paceleague.common.config.JwtProperties;
import com.paceleague.member.application.port.out.MemberAgreementRepositoryPort;
import com.paceleague.member.application.port.out.MemberRepositoryPort;
import com.paceleague.member.application.port.out.RefreshTokenStorePort;
import com.paceleague.member.application.port.out.TokenIssuerPort;
import com.paceleague.member.domain.entity.AgreementType;
import com.paceleague.member.domain.entity.Member;
import com.paceleague.member.domain.entity.MemberAgreement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberAuthServiceJoinTest {

    @Mock MemberRepositoryPort memberRepositoryPort;
    @Mock MemberAgreementRepositoryPort memberAgreementRepositoryPort;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RefreshTokenStorePort refreshTokenStorePort;
    @Mock TokenIssuerPort tokenIssuerPort;
    @Mock StringRedisTemplate redis;

    MemberAuthService service;

    @BeforeEach
    void setUp() {
        service = new MemberAuthService(memberRepositoryPort, memberAgreementRepositoryPort, passwordEncoder,
                refreshTokenStorePort, tokenIssuerPort, new JwtProperties("iss", "secret", 1800, 1209600), redis);
        when(passwordEncoder.encode("password1")).thenReturn("HASH");
        when(memberRepositoryPort.save(any())).thenAnswer(inv -> {
            Member m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "sno", 42);
            return m;
        });
    }

    private Map<AgreementType, Boolean> join(Boolean push, Boolean marketing, Boolean night) {
        service.join("runner1", "password1", "달리는곰", null, true, true, true, push, marketing, night);
        ArgumentCaptor<MemberAgreement> captor = ArgumentCaptor.forClass(MemberAgreement.class);
        verify(memberAgreementRepositoryPort, atLeastOnce()).save(captor.capture());
        Map<AgreementType, Boolean> saved = new EnumMap<>(AgreementType.class);
        captor.getAllValues().forEach(a -> saved.put(a.getAgreementType(), a.isAgreed()));
        return saved;
    }

    @Test
    void 가입때_보낸_알림_동의_3종이_필수_동의와_함께_기록된다() {
        Map<AgreementType, Boolean> saved = join(true, true, false);

        assertThat(saved).containsEntry(AgreementType.TERMS, true)
                .containsEntry(AgreementType.PRIVACY, true)
                .containsEntry(AgreementType.LOCATION, true)
                .containsEntry(AgreementType.PUSH_SERVICE, true)
                .containsEntry(AgreementType.MARKETING, true)
                .containsEntry(AgreementType.MARKETING_NIGHT, false);
    }

    @Test
    void 알림_동의를_보내지_않으면_기록하지_않아_첫_로그인때_다시_묻는다() {
        Map<AgreementType, Boolean> saved = join(null, null, null);

        assertThat(saved).containsOnlyKeys(AgreementType.TERMS, AgreementType.PRIVACY, AgreementType.LOCATION);
    }

    @Test
    void 마케팅_미동의면_야간_수신은_true로_보내도_false로_기록된다() {
        Map<AgreementType, Boolean> saved = join(false, false, true);

        assertThat(saved).containsEntry(AgreementType.MARKETING, false)
                .containsEntry(AgreementType.MARKETING_NIGHT, false);
    }

    @Test
    void 필수_동의가_빠지면_가입되지_않는다() {
        assertThatThrownBy(() -> service.join("runner1", "password1", null, null,
                true, true, false, true, true, true))
                .isInstanceOf(IllegalArgumentException.class);

        verify(memberRepositoryPort, never()).save(any());
        verify(memberAgreementRepositoryPort, never()).save(any());
    }
}

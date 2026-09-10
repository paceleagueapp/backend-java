package com.paceleague.admin.application.service;

import com.paceleague.admin.application.dto.AdminSessionInfo;
import com.paceleague.admin.application.port.out.AdminRepositoryPort;
import com.paceleague.admin.application.port.out.AdminSessionStorePort;
import com.paceleague.admin.domain.entity.Admin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminAuthServiceTest {

    @Mock AdminRepositoryPort adminRepositoryPort;
    @Mock AdminSessionStorePort adminSessionStorePort;
    @Mock PasswordEncoder passwordEncoder;

    AdminAuthService service() {
        return new AdminAuthService(adminRepositoryPort, adminSessionStorePort, passwordEncoder);
    }

    @Test
    void 존재하지_않는_아이디면_로그인에_실패한다() {
        when(adminRepositoryPort.findByAdminId("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().login("nobody", "pw"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(adminSessionStorePort, never()).issue(anyLong());
    }

    @Test
    void 비밀번호가_틀리면_로그인에_실패한다() {
        Admin admin = Admin.create("root", "hashed");
        when(adminRepositoryPort.findByAdminId("root")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service().login("root", "wrong"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(adminSessionStorePort, never()).issue(anyLong());
    }

    @Test
    void 아이디와_비밀번호가_맞으면_세션을_발급한다() {
        Admin admin = Admin.create("root", "hashed");
        when(adminRepositoryPort.findByAdminId("root")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(adminSessionStorePort.issue(admin.getSno())).thenReturn("session-token");

        AdminSessionInfo info = service().login("root", "correct");

        assertThat(info.sessionToken()).isEqualTo("session-token");
        assertThat(info.adminId()).isEqualTo("root");
    }

    @Test
    void 로그아웃은_세션을_무효화한다() {
        service().logout("session-token");
        verify(adminSessionStorePort).revoke("session-token");
    }

    @Test
    void 빈_토큰으로_로그아웃하면_아무것도_하지_않는다() {
        service().logout(null);
        verify(adminSessionStorePort, never()).revoke(anyString());
    }
}

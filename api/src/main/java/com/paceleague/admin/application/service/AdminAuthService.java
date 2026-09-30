package com.paceleague.admin.application.service;

import com.paceleague.admin.application.dto.AdminSessionInfo;
import com.paceleague.admin.application.port.in.AdminAuthUseCase;
import com.paceleague.admin.application.port.out.AdminRepositoryPort;
import com.paceleague.admin.application.port.out.AdminSessionStorePort;
import com.paceleague.admin.domain.entity.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService implements AdminAuthUseCase {

    private final AdminRepositoryPort adminRepositoryPort;
    private final AdminSessionStorePort adminSessionStorePort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminSessionInfo login(String adminId, String rawPassword) {
        Admin admin = adminRepositoryPort.findByAdminId(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Incorrect username or password."));

        if (!passwordEncoder.matches(rawPassword, admin.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect username or password.");
        }

        String token = adminSessionStorePort.issue(admin.getSno());
        return new AdminSessionInfo(token, admin.getAdminId());
    }

    @Override
    public void logout(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            return;
        }
        adminSessionStorePort.revoke(sessionToken);
    }
}

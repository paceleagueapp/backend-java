package com.paceleague.admin.application.port.in;

import com.paceleague.admin.application.dto.AdminSessionInfo;

public interface AdminAuthUseCase {
    AdminSessionInfo login(String adminId, String rawPassword);

    void logout(String sessionToken);
}

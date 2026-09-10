package com.paceleague.admin.application.port.out;

import com.paceleague.admin.domain.entity.Admin;

import java.util.Optional;

public interface AdminRepositoryPort {
    Optional<Admin> findByAdminId(String adminId);
}

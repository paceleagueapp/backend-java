package com.paceleague.admin.adapter.out.persistence;

import com.paceleague.admin.application.port.out.AdminRepositoryPort;
import com.paceleague.admin.domain.entity.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminPersistenceAdapter implements AdminRepositoryPort {

    private final AdminJpaRepository adminJpaRepository;

    public Optional<Admin> findByAdminId(String adminId) {
        return adminJpaRepository.findByAdminId(adminId);
    }
}

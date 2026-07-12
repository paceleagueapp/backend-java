package com.example.paceleague.appversion.repository;

import com.example.paceleague.appversion.entity.AppVersionPolicy;
import com.example.paceleague.appversion.enums.AppPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppVersionPolicyRepository extends JpaRepository<AppVersionPolicy, Long> {

    Optional<AppVersionPolicy> findByPlatform(AppPlatform platform);
}
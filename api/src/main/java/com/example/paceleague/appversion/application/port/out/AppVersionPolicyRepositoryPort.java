package com.example.paceleague.appversion.application.port.out;

import com.example.paceleague.appversion.domain.entity.AppVersionPolicy;
import com.example.paceleague.appversion.domain.enums.AppPlatform;

import java.util.Optional;

public interface AppVersionPolicyRepositoryPort {
    Optional<AppVersionPolicy> findByPlatform(AppPlatform platform);
}

package com.paceleague.appversion.application.port.out;

import com.paceleague.appversion.domain.entity.AppVersionPolicy;
import com.paceleague.appversion.domain.enums.AppPlatform;

import java.util.Optional;

public interface AppVersionPolicyRepositoryPort {
    Optional<AppVersionPolicy> findByPlatform(AppPlatform platform);
}

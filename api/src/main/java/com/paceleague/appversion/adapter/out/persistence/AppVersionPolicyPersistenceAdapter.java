package com.paceleague.appversion.adapter.out.persistence;

import com.paceleague.appversion.application.port.out.AppVersionPolicyRepositoryPort;
import com.paceleague.appversion.domain.entity.AppVersionPolicy;
import com.paceleague.appversion.domain.enums.AppPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AppVersionPolicyPersistenceAdapter implements AppVersionPolicyRepositoryPort {

    private final AppVersionPolicyJpaRepository appVersionPolicyJpaRepository;

    public Optional<AppVersionPolicy> findByPlatform(AppPlatform platform) {
        return appVersionPolicyJpaRepository.findByPlatform(platform);
    }
}

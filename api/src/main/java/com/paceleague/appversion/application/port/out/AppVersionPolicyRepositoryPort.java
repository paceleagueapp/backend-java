package com.paceleague.appversion.application.port.out;

import com.paceleague.appversion.domain.entity.AppVersionPolicy;
import com.paceleague.appversion.domain.enums.AppPlatform;

import java.util.List;
import java.util.Optional;

public interface AppVersionPolicyRepositoryPort {
    Optional<AppVersionPolicy> findByPlatform(AppPlatform platform);

    // 관리자 설정 화면 — 등록된 플랫폼(들) 전체.
    List<AppVersionPolicy> findAll();

    AppVersionPolicy save(AppVersionPolicy policy);
}

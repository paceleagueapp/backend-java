package com.example.paceleague.appversion.adapter.out.persistence;

import com.example.paceleague.appversion.domain.entity.AppVersionPolicy;
import com.example.paceleague.appversion.domain.enums.AppPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppVersionPolicyJpaRepository extends JpaRepository<AppVersionPolicy, Long> {

    Optional<AppVersionPolicy> findByPlatform(AppPlatform platform);
}

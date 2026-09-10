package com.paceleague.appversion.application.dto;

import com.paceleague.appversion.domain.enums.AppPlatform;

import java.time.LocalDateTime;

public record AppVersionSummary(
        AppPlatform platform,
        String latestVersion,
        String minRequiredVersion,
        LocalDateTime updateAt
) {}

package com.example.paceleague.appversion.application.port.in;

import com.example.paceleague.appversion.application.dto.AppVersionCheckResponse;
import com.example.paceleague.appversion.domain.enums.AppPlatform;

public interface CheckAppVersionUseCase {
    AppVersionCheckResponse checkVersion(AppPlatform platform, String currentVersion);
}

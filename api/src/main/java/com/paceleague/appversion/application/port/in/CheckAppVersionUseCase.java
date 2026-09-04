package com.paceleague.appversion.application.port.in;

import com.paceleague.appversion.application.dto.AppVersionCheckResponse;
import com.paceleague.appversion.domain.enums.AppPlatform;

public interface CheckAppVersionUseCase {
    AppVersionCheckResponse checkVersion(AppPlatform platform, String currentVersion);
}

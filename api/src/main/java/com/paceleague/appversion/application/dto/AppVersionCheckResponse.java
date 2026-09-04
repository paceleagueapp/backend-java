package com.paceleague.appversion.application.dto;

import com.paceleague.appversion.domain.enums.AppPlatform;
import com.paceleague.appversion.domain.enums.AppUpdateType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppVersionCheckResponse {

    private AppPlatform platform;

    private String currentVersion;

    private String latestVersion;

    private String minRequiredVersion;

    private AppUpdateType updateType;

    private Boolean forceUpdate;

    private String storeUrl;

    private String message;

    private Boolean maintenance;

    private String maintenanceMessage;
}

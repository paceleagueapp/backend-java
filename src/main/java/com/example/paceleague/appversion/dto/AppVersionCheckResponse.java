package com.example.paceleague.appversion.dto;

import com.example.paceleague.appversion.enums.AppPlatform;
import com.example.paceleague.appversion.enums.AppUpdateType;
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
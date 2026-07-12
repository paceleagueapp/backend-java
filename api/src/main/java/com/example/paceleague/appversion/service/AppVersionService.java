package com.example.paceleague.appversion.service;

import com.example.paceleague.appversion.dto.AppVersionCheckResponse;
import com.example.paceleague.appversion.entity.AppVersionPolicy;
import com.example.paceleague.appversion.enums.AppPlatform;
import com.example.paceleague.appversion.enums.AppUpdateType;
import com.example.paceleague.appversion.repository.AppVersionPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionPolicyRepository appVersionPolicyRepository;

    public AppVersionCheckResponse checkVersion(AppPlatform platform, String currentVersion) {
        AppVersionPolicy policy = appVersionPolicyRepository.findByPlatform(platform)
                .orElseThrow(() -> new IllegalArgumentException("앱 버전 정책이 존재하지 않습니다. platform=" + platform));

        AppUpdateType updateType = getUpdateType(
                currentVersion,
                policy.getMinRequiredVersion(),
                policy.getLatestVersion()
        );

        boolean maintenance = "Y".equalsIgnoreCase(policy.getMaintenanceYn());

        return AppVersionCheckResponse.builder()
                .platform(platform)
                .currentVersion(currentVersion)
                .latestVersion(policy.getLatestVersion())
                .minRequiredVersion(policy.getMinRequiredVersion())
                .updateType(updateType)
                .forceUpdate(updateType == AppUpdateType.FORCE)
                .storeUrl(policy.getStoreUrl())
                .message(policy.getUpdateMessage())
                .maintenance(maintenance)
                .maintenanceMessage(policy.getMaintenanceMessage())
                .build();
    }

    private AppUpdateType getUpdateType(String currentVersion, String minRequiredVersion, String latestVersion) {
        if (compareVersion(currentVersion, minRequiredVersion) < 0) {
            return AppUpdateType.FORCE;
        }

        if (compareVersion(currentVersion, latestVersion) < 0) {
            return AppUpdateType.OPTIONAL;
        }

        return AppUpdateType.NONE;
    }

    private int compareVersion(String v1, String v2) {
        String[] arr1 = v1.split("\\.");
        String[] arr2 = v2.split("\\.");

        int length = Math.max(arr1.length, arr2.length);

        for (int i = 0; i < length; i++) {
            int num1 = i < arr1.length ? Integer.parseInt(arr1[i]) : 0;
            int num2 = i < arr2.length ? Integer.parseInt(arr2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }

        return 0;
    }
}
package com.example.paceleague.appversion.controller;

import com.example.paceleague.appversion.dto.AppVersionCheckResponse;
import com.example.paceleague.appversion.enums.AppPlatform;
import com.example.paceleague.appversion.service.AppVersionService;
import com.example.paceleague.common.response.ResponseApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
public class AppVersionController {

    private final AppVersionService appVersionService;

    @GetMapping("/version-check")
    public ResponseApi<AppVersionCheckResponse> checkVersion(
            @RequestParam AppPlatform platform,
            @RequestParam String appVersion
    ) {
        AppVersionCheckResponse response = appVersionService.checkVersion(platform, appVersion);

        return ResponseApi.success(response);
    }
}
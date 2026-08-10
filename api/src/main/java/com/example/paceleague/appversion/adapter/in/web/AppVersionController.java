package com.example.paceleague.appversion.adapter.in.web;

import com.example.paceleague.appversion.application.dto.AppVersionCheckResponse;
import com.example.paceleague.appversion.application.port.in.CheckAppVersionUseCase;
import com.example.paceleague.appversion.domain.enums.AppPlatform;
import com.example.paceleague.common.response.ResponseApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
@Tag(name = "AppVersion", description = "모바일 앱 강제/선택 업데이트 및 점검 여부 체크 API")
public class AppVersionController {

    private final CheckAppVersionUseCase checkAppVersionUseCase;

    @Operation(summary = "앱 버전 체크", description = "플랫폼별 최신/최소 버전과 비교해 강제/선택 업데이트 및 점검 여부를 반환합니다. 인증이 필요 없습니다.")
    @ApiResponse(responseCode = "200", description = "체크 성공")
    @GetMapping("/version-check")
    public ResponseApi<AppVersionCheckResponse> checkVersion(
            @Parameter(description = "플랫폼") @RequestParam AppPlatform platform,
            @Parameter(description = "클라이언트 현재 앱 버전 (x.y.z)") @RequestParam String appVersion
    ) {
        AppVersionCheckResponse response = checkAppVersionUseCase.checkVersion(platform, appVersion);

        return ResponseApi.success(response);
    }
}

package com.paceleague.admin.adapter.in.web;

import com.paceleague.admin.application.dto.AppVersionUpdateRequest;
import com.paceleague.appversion.application.dto.AppVersionSummary;
import com.paceleague.appversion.application.port.in.shared.AdminAppVersionUseCase;
import com.paceleague.appversion.domain.enums.AppPlatform;
import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
@Tag(name = "Admin - Settings", description = "관리자 설정 화면 API — 안드로이드/iOS 앱 버전")
@SecurityRequirement(name = "adminSession")
public class AdminSettingsController {

    private final AdminAppVersionUseCase adminAppVersionUseCase;

    public AdminSettingsController(AdminAppVersionUseCase adminAppVersionUseCase) {
        this.adminAppVersionUseCase = adminAppVersionUseCase;
    }

    @Operation(summary = "앱 버전 정책 목록", description = "등록된 플랫폼(안드로이드/iOS)의 현재 버전 설정.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/app-version")
    public ResponseApi<List<AppVersionSummary>> listAppVersions(@AdminSno Long adminSno) {
        return ResponseApi.success(adminAppVersionUseCase.list());
    }

    @Operation(summary = "앱 버전 설정 변경", description = "해당 플랫폼의 최신/최소 버전을 저장(없으면 새로 생성).")
    @ApiResponse(responseCode = "200", description = "저장 성공")
    @PutMapping("/app-version/{platform}")
    public ResponseApi<AppVersionSummary> updateAppVersion(
            @AdminSno Long adminSno,
            @PathVariable AppPlatform platform,
            @Valid @RequestBody AppVersionUpdateRequest req
    ) {
        var result = adminAppVersionUseCase.updateVersion(platform, req.latestVersion(), req.minRequiredVersion());
        return ResponseApi.success("저장되었습니다.", result);
    }
}

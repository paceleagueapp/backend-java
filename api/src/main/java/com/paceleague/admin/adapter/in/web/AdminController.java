package com.paceleague.admin.adapter.in.web;

import com.paceleague.admin.application.dto.AdminLoginRequest;
import com.paceleague.admin.application.dto.AdminMeResponse;
import com.paceleague.admin.application.dto.AdminSessionInfo;
import com.paceleague.admin.application.dto.AdminSessionResponse;
import com.paceleague.admin.application.port.in.AdminAuthUseCase;
import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.security.AdminSessionFilter;
import com.paceleague.common.web.AdminSno;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "관리자 로그인/세션 API — member 인증과 완전히 별개")
public class AdminController {

    private final AdminAuthUseCase authService;

    public AdminController(AdminAuthUseCase authService) {
        this.authService = authService;
    }

    @Operation(summary = "관리자 로그인", description = "아이디/비밀번호 확인 후 Redis 기반 세션 토큰 발급(X-Admin-Session 헤더로 사용).")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/login")
    public ResponseApi<AdminSessionResponse> login(@Valid @RequestBody AdminLoginRequest req) {
        AdminSessionInfo info = authService.login(req.adminId(), req.password());
        return ResponseApi.success("로그인 성공", new AdminSessionResponse(info.sessionToken(), info.adminId()));
    }

    @Operation(summary = "관리자 로그아웃", description = "세션을 즉시 무효화한다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseApi<String> logout(@RequestHeader(value = AdminSessionFilter.SESSION_HEADER, required = false) String sessionToken) {
        authService.logout(sessionToken);
        return ResponseApi.success("로그아웃되었습니다.");
    }

    @Operation(summary = "세션 확인", description = "관리자 페이지 진입 시 세션 유효성을 확인하는 용도. 세션 없거나 만료되면 401.")
    @ApiResponse(responseCode = "200", description = "유효한 세션")
    @SecurityRequirement(name = "adminSession")
    @GetMapping("/me")
    public ResponseApi<AdminMeResponse> me(@AdminSno Long adminSno) {
        return ResponseApi.success(new AdminMeResponse(adminSno));
    }
}

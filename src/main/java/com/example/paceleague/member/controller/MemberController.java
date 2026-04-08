package com.example.paceleague.member.controller;

import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.member.dto.*;
import com.example.paceleague.member.service.MemberAuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@Tag(name = "Member", description = "회원 인증 API")
public class MemberController {
    private final MemberAuthServiceImpl authService;

    public MemberController(MemberAuthServiceImpl authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "회원가입 후 access/refresh token 발급")
    @ApiResponse(responseCode = "200", description = "회원가입 성공")
    @PostMapping("/join")
    public ResponseEntity<ResponseApi<TokenResponse>> join(@Valid @RequestBody JoinRequest req) {

        AuthTokenInfo result = authService.join(
                req.memberId(),
                req.password(),
                req.nickname(),
                req.email()
        );

        return ResponseEntity.ok(
                ResponseApi.success("회원가입 성공", toTokenResponse(result))
        );
    }

    @Operation(summary = "로그인", description = "memberId/password로 로그인 후 access/refresh token 발급")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/login")
    public ResponseEntity<ResponseApi<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthTokenInfo result = authService.login(req.memberId(), req.password());

        return ResponseEntity.ok(
                ResponseApi.success("로그인 성공", toTokenResponse(result))
        );
    }

    @Operation(summary = "토큰 재발급", description = "refresh token으로 access token 재발급")
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @PostMapping("/reissue")
    public ResponseEntity<ResponseApi<TokenResponse>> reissue(@RequestBody TokenReissueRequest req) {
        AuthTokenInfo result = authService.reissue(req.refreshToken());

        return ResponseEntity.ok(
                ResponseApi.success("토큰 재발급 성공", toTokenResponse(result))
        );
    }

    @Operation(summary = "로그아웃", description = "회원 로그아웃 후 token 폐기")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<ResponseApi<String>> logout(@RequestBody LogoutRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.ok(ResponseApi.success("로그아웃이 완료되었습니다."));
    }

    private TokenResponse toTokenResponse(AuthTokenInfo result) {
        return new TokenResponse(
                result.grantType(),
                result.accessToken(),
                result.accessTokenExpiresIn(),
                result.refreshToken(),
                result.refreshTokenExpiresIn()
        );
    }
}

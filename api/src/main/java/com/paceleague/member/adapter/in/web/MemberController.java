package com.paceleague.member.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.MemberSno;
import com.paceleague.member.application.dto.*;
import com.paceleague.member.application.port.in.MemberAuthUseCase;
import com.paceleague.member.application.port.in.SearchMembersPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@Tag(name = "Member", description = "회원 인증 API")
public class MemberController {
    private final MemberAuthUseCase authService;
    private final SearchMembersPort searchMembersPort;

    public MemberController(MemberAuthUseCase authService, SearchMembersPort searchMembersPort) {
        this.authService = authService;
        this.searchMembersPort = searchMembersPort;
    }

    @Operation(summary = "회원 검색", description = "아이디(접두 일치) 또는 닉네임(부분 일치)으로 회원을 찾습니다. 크루 초대 대상 선택 등에 사용. 로그인 필요.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    public ResponseApi<List<MemberSearchResult>> search(
            @MemberSno Long memberSno,
            @Parameter(description = "검색어(아이디/닉네임)") @RequestParam String q
    ) {
        return ResponseApi.success(searchMembersPort.search(q, 20));
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
                result.refreshTokenExpiresIn(),
                result.nickname()
        );
    }
}

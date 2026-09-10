package com.paceleague.member.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.MemberSno;
import com.paceleague.member.application.dto.*;
import com.paceleague.member.application.port.in.MemberAgreementUseCase;
import com.paceleague.member.application.port.in.MemberAuthUseCase;
import com.paceleague.member.application.port.in.MemberBlockUseCase;
import com.paceleague.member.application.port.in.MemberWithdrawUseCase;
import com.paceleague.member.application.port.in.SearchMembersPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final MemberWithdrawUseCase withdrawService;
    private final MemberBlockUseCase blockService;
    private final MemberAgreementUseCase agreementService;

    public MemberController(MemberAuthUseCase authService, SearchMembersPort searchMembersPort,
                           MemberWithdrawUseCase withdrawService, MemberBlockUseCase blockService,
                           MemberAgreementUseCase agreementService) {
        this.authService = authService;
        this.searchMembersPort = searchMembersPort;
        this.withdrawService = withdrawService;
        this.blockService = blockService;
        this.agreementService = agreementService;
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

    @Operation(summary = "회원가입", description = "이용약관/개인정보처리방침/위치정보 수집·이용에 모두 동의해야 가입 가능. 회원가입 후 access/refresh token 발급")
    @ApiResponse(responseCode = "200", description = "회원가입 성공")
    @PostMapping("/join")
    public ResponseEntity<ResponseApi<TokenResponse>> join(@Valid @RequestBody JoinRequest req) {

        AuthTokenInfo result = authService.join(
                req.memberId(),
                req.password(),
                req.nickname(),
                req.email(),
                req.agreedTerms(),
                req.agreedPrivacy(),
                req.agreedLocation()
        );

        return ResponseEntity.ok(
                ResponseApi.success("회원가입 성공", toTokenResponse(result))
        );
    }

    @Operation(summary = "내 동의 상태 조회", description = "약관/개인정보/위치정보/알림(서비스·마케팅·야간마케팅) 동의 상태를 모두 반환. 알림 3종은 아직 응답(answered=false)이 없으면 로그인 시 동의 화면으로 보내는 용도. 로그인 필요.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/agreements")
    public ResponseApi<List<AgreementStatusResponse>> agreements(@MemberSno Long memberSno) {
        return ResponseApi.success(agreementService.getStatus(memberSno));
    }

    @Operation(summary = "동의 상태 갱신", description = "알림(서비스/마케팅/야간마케팅) 동의를 로그인 직후 동의 화면 또는 계정설정에서 저장/변경할 때 사용. 로그인 필요.")
    @ApiResponse(responseCode = "200", description = "저장 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/agreements")
    public ResponseApi<String> updateAgreements(@MemberSno Long memberSno, @Valid @RequestBody AgreementUpdateRequest req) {
        agreementService.updateAgreements(memberSno, req.agreements());
        return ResponseApi.success("저장되었습니다.");
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

    @Operation(summary = "회원 탈퇴", description = "비밀번호 재확인 후 즉시 처리됩니다. 러닝·건강·랭킹·땅 데이터는 삭제되고, 작성한 글/댓글은 '탈퇴한 사용자'로 익명화되어 남습니다. **복구 불가.** 크루장은 먼저 위임/해체해야 합니다. 로그인 필요.")
    @ApiResponse(responseCode = "200", description = "탈퇴 성공")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    public ResponseApi<String> withdraw(@MemberSno Long memberSno, @RequestBody WithdrawRequest req) {
        withdrawService.withdraw(memberSno, req.password());
        return ResponseApi.success("탈퇴가 완료되었습니다.");
    }

    @Operation(summary = "회원 차단", description = "차단한 회원의 게시글/댓글이 내 피드에서 보이지 않습니다(단방향). 중복 요청은 멱등. 로그인 필요.")
    @ApiResponse(responseCode = "200", description = "차단 완료")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/blocks")
    public ResponseApi<String> block(@MemberSno Long memberSno, @RequestBody BlockRequest req) {
        blockService.block(memberSno, req.blockedMemberSno());
        return ResponseApi.success("차단했습니다.");
    }

    @Operation(summary = "회원 차단 해제", description = "로그인 필요.")
    @ApiResponse(responseCode = "200", description = "해제 완료")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/blocks/{blockedMemberSno}")
    public ResponseApi<String> unblock(@MemberSno Long memberSno, @PathVariable Long blockedMemberSno) {
        blockService.unblock(memberSno, blockedMemberSno);
        return ResponseApi.success("차단을 해제했습니다.");
    }

    @Operation(summary = "내 차단 목록", description = "로그인 필요.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/blocks")
    public ResponseApi<List<BlockedMemberResponse>> blocks(@MemberSno Long memberSno) {
        return ResponseApi.success(blockService.list(memberSno));
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

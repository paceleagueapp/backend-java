package com.example.paceleague.crew.adapter.in.web;

import com.example.paceleague.common.i18n.LocaleResolver;
import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.web.MemberSno;
import com.example.paceleague.crew.application.dto.*;
import com.example.paceleague.crew.application.port.in.CrewCommandUseCase;
import com.example.paceleague.crew.application.port.in.CrewInvitationUseCase;
import com.example.paceleague.crew.application.port.in.CrewJoinRequestUseCase;
import com.example.paceleague.crew.application.port.in.CrewQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crew")
@RequiredArgsConstructor
@Tag(name = "Crew", description = "크루(길드) API — 생성/검색/초대/가입신청/크루원 관리. 검색·상세는 비로그인 가능, 나머지는 로그인 필요.")
public class CrewController {

    private final CrewCommandUseCase crewCommandUseCase;
    private final CrewQueryUseCase crewQueryUseCase;
    private final CrewInvitationUseCase crewInvitationUseCase;
    private final CrewJoinRequestUseCase crewJoinRequestUseCase;

    // ── 크루 CRUD ──────────────────────────────────────────────────────────────

    @Operation(summary = "크루 생성", description = "크루 없는 회원만. 생성자가 크루장 + 첫 크루원이 됩니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseApi<CrewCreatedResponse> create(@MemberSno Long memberSno, @RequestBody CrewCreateRequest req) {
        return ResponseApi.success("크루가 생성되었습니다.", new CrewCreatedResponse(crewCommandUseCase.create(memberSno, req)));
    }

    @Operation(summary = "크루 검색", description = "인증 불필요. 크루명 부분 일치. q 가 없으면 이름순 목록.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirements
    @GetMapping("/search")
    public ResponseApi<List<CrewSummaryResponse>> search(
            @Parameter(description = "크루명 검색어") @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam(required = false) String country) {
        return ResponseApi.success(crewQueryUseCase.search(q, resolveLang(lang, country)));
    }

    @Operation(summary = "내 크루 조회", description = "크루가 없으면 data 가 null 입니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseApi<CrewDetailResponse> getMyCrew(
            @MemberSno Long memberSno,
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam(required = false) String country) {
        return ResponseApi.success(crewQueryUseCase.getMyCrew(memberSno, resolveLang(lang, country)));
    }

    @Operation(summary = "크루 상세 조회", description = "로그인 필요. 요청자가 크루원이면 공지·크루원 목록이 함께 옵니다(아니면 공개 정보만).")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{crewSno}")
    public ResponseApi<CrewDetailResponse> getDetail(
            @MemberSno Long memberSno,
            @PathVariable Long crewSno,
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam(required = false) String country) {
        return ResponseApi.success(crewQueryUseCase.getDetail(crewSno, memberSno, resolveLang(lang, country)));
    }

    @Operation(summary = "크루원 랭킹", description = "크루원만. 크루 내 회원들을 현재 시즌 점수 기준으로 정렬한 랭킹.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{crewSno}/ranking")
    public ResponseApi<List<CrewRankingEntryResponse>> getRanking(
            @MemberSno Long memberSno,
            @PathVariable Long crewSno,
            @RequestParam(defaultValue = "ko") String lang,
            @RequestParam(required = false) String country) {
        return ResponseApi.success(crewQueryUseCase.getRanking(memberSno, crewSno, resolveLang(lang, country)));
    }

    @Operation(summary = "크루 정보 수정", description = "크루장만. 이름·아이콘·소개·공지를 통째로 갱신합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{crewSno}")
    public ResponseApi<String> update(@MemberSno Long memberSno, @PathVariable Long crewSno,
                                      @RequestBody CrewUpdateRequest req) {
        crewCommandUseCase.update(memberSno, crewSno, req);
        return ResponseApi.success("크루 정보가 수정되었습니다.");
    }

    @Operation(summary = "크루 해체", description = "크루장만. 크루원이 혼자 남았을 때만 가능합니다.")
    @ApiResponse(responseCode = "200", description = "해체 성공")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{crewSno}")
    public ResponseApi<String> disband(@MemberSno Long memberSno, @PathVariable Long crewSno) {
        crewCommandUseCase.disband(memberSno, crewSno);
        return ResponseApi.success("크루가 해체되었습니다.");
    }

    // ── 크루원 관리 ────────────────────────────────────────────────────────────

    @Operation(summary = "크루 탈퇴", description = "크루원 본인. 크루장은 위임 또는 해체 후에만 탈퇴 가능.")
    @ApiResponse(responseCode = "200", description = "탈퇴 성공")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{crewSno}/members/me")
    public ResponseApi<String> leave(@MemberSno Long memberSno, @PathVariable Long crewSno) {
        crewCommandUseCase.leave(memberSno, crewSno);
        return ResponseApi.success("크루에서 탈퇴했습니다.");
    }

    @Operation(summary = "크루원 추방", description = "크루장만.")
    @ApiResponse(responseCode = "200", description = "추방 성공")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{crewSno}/members/{targetMemberSno}")
    public ResponseApi<String> kick(@MemberSno Long memberSno, @PathVariable Long crewSno,
                                    @PathVariable Long targetMemberSno) {
        crewCommandUseCase.kick(memberSno, crewSno, targetMemberSno);
        return ResponseApi.success("크루원을 내보냈습니다.");
    }

    @Operation(summary = "크루장 위임", description = "크루장만. 기존 크루장은 일반 크루원이 됩니다.")
    @ApiResponse(responseCode = "200", description = "위임 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{crewSno}/leader")
    public ResponseApi<String> transferLeader(@MemberSno Long memberSno, @PathVariable Long crewSno,
                                              @RequestBody CrewInviteRequest req) {
        crewCommandUseCase.transferLeader(memberSno, crewSno, req.inviteeMemberSno());
        return ResponseApi.success("크루장을 위임했습니다.");
    }

    // ── 초대 ─────────────────────────────────────────────────────────────────

    @Operation(summary = "회원 초대", description = "크루장만. GET /api/member/search 로 찾은 회원의 memberSno.")
    @ApiResponse(responseCode = "200", description = "초대 발송")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{crewSno}/invitations")
    public ResponseApi<CrewCreatedResponse> invite(@MemberSno Long memberSno, @PathVariable Long crewSno,
                                                   @RequestBody CrewInviteRequest req) {
        return ResponseApi.success("초대를 보냈습니다.",
                new CrewCreatedResponse(crewInvitationUseCase.invite(memberSno, crewSno, req.inviteeMemberSno())));
    }

    @Operation(summary = "내가 받은 초대 목록")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/invitations/me")
    public ResponseApi<List<CrewInvitationResponse>> myInvitations(@MemberSno Long memberSno) {
        return ResponseApi.success(crewInvitationUseCase.listMyInvitations(memberSno));
    }

    @Operation(summary = "초대 수락", description = "초대받은 회원 본인. 수락 시 크루에 가입됩니다.")
    @ApiResponse(responseCode = "200", description = "수락 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseApi<String> acceptInvitation(@MemberSno Long memberSno, @PathVariable Long invitationId) {
        crewInvitationUseCase.accept(memberSno, invitationId);
        return ResponseApi.success("크루에 가입했습니다.");
    }

    @Operation(summary = "초대 거절")
    @ApiResponse(responseCode = "200", description = "거절 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/invitations/{invitationId}/decline")
    public ResponseApi<String> declineInvitation(@MemberSno Long memberSno, @PathVariable Long invitationId) {
        crewInvitationUseCase.decline(memberSno, invitationId);
        return ResponseApi.success("초대를 거절했습니다.");
    }

    @Operation(summary = "보낸 초대 취소", description = "크루장만.")
    @ApiResponse(responseCode = "200", description = "취소 성공")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/invitations/{invitationId}")
    public ResponseApi<String> cancelInvitation(@MemberSno Long memberSno, @PathVariable Long invitationId) {
        crewInvitationUseCase.cancel(memberSno, invitationId);
        return ResponseApi.success("초대를 취소했습니다.");
    }

    // ── 가입신청 ──────────────────────────────────────────────────────────────

    @Operation(summary = "가입신청", description = "크루 없는 회원. 크루장이 승인하면 가입됩니다.")
    @ApiResponse(responseCode = "200", description = "신청 완료")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{crewSno}/join-requests")
    public ResponseApi<CrewCreatedResponse> apply(@MemberSno Long memberSno, @PathVariable Long crewSno,
                                                  @RequestBody CrewJoinApplyRequest req) {
        return ResponseApi.success("가입신청을 보냈습니다.",
                new CrewCreatedResponse(crewJoinRequestUseCase.apply(memberSno, crewSno, req.message())));
    }

    @Operation(summary = "가입신청 목록", description = "크루장만. 우리 크루로 온 대기 중 신청.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{crewSno}/join-requests")
    public ResponseApi<List<CrewJoinRequestResponse>> listJoinRequests(@MemberSno Long memberSno,
                                                                       @PathVariable Long crewSno) {
        return ResponseApi.success(crewJoinRequestUseCase.listPending(memberSno, crewSno));
    }

    @Operation(summary = "가입신청 승인", description = "크루장만.")
    @ApiResponse(responseCode = "200", description = "승인 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/join-requests/{joinRequestId}/approve")
    public ResponseApi<String> approveJoinRequest(@MemberSno Long memberSno, @PathVariable Long joinRequestId) {
        crewJoinRequestUseCase.approve(memberSno, joinRequestId);
        return ResponseApi.success("가입신청을 승인했습니다.");
    }

    @Operation(summary = "가입신청 거절", description = "크루장만.")
    @ApiResponse(responseCode = "200", description = "거절 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/join-requests/{joinRequestId}/reject")
    public ResponseApi<String> rejectJoinRequest(@MemberSno Long memberSno, @PathVariable Long joinRequestId) {
        crewJoinRequestUseCase.reject(memberSno, joinRequestId);
        return ResponseApi.success("가입신청을 거절했습니다.");
    }

    @Operation(summary = "가입신청 취소", description = "신청자 본인.")
    @ApiResponse(responseCode = "200", description = "취소 성공")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/join-requests/{joinRequestId}")
    public ResponseApi<String> cancelJoinRequest(@MemberSno Long memberSno, @PathVariable Long joinRequestId) {
        crewJoinRequestUseCase.cancel(memberSno, joinRequestId);
        return ResponseApi.success("가입신청을 취소했습니다.");
    }

    private String resolveLang(String lang, String country) {
        return LocaleResolver.resolve(lang, country).toCode();
    }
}

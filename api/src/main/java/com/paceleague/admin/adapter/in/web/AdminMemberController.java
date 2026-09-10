package com.paceleague.admin.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import com.paceleague.member.application.dto.AdminMemberDetail;
import com.paceleague.member.application.dto.AdminMemberSummary;
import com.paceleague.member.application.port.in.shared.AdminMemberQueryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@Tag(name = "Admin - Member", description = "관리자 회원관리 화면 API")
@SecurityRequirement(name = "adminSession")
public class AdminMemberController {

    private final AdminMemberQueryPort adminMemberQueryPort;

    public AdminMemberController(AdminMemberQueryPort adminMemberQueryPort) {
        this.adminMemberQueryPort = adminMemberQueryPort;
    }

    @Operation(summary = "회원 목록", description = "아이디/닉네임 검색 + 페이지네이션. q가 비어있으면 전체(탈퇴 회원 포함) 최신 가입순.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseApi<Page<AdminMemberSummary>> list(
            @AdminSno Long adminSno,
            @Parameter(description = "아이디/닉네임 검색어") @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseApi.success(adminMemberQueryPort.search(q, page, size));
    }

    @Operation(summary = "회원 상세", description = "기본 정보 + 현재 푸시 알림(서비스 알림) 동의 여부.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{memberSno}")
    public ResponseApi<AdminMemberDetail> detail(@AdminSno Long adminSno, @PathVariable Long memberSno) {
        return ResponseApi.success(adminMemberQueryPort.getDetail(memberSno));
    }
}

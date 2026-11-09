package com.paceleague.admin.adapter.in.web;

import com.paceleague.admin.application.dto.AdminNotificationSendRequest;
import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import com.paceleague.notification.application.dto.AdminNotificationDetail;
import com.paceleague.notification.application.dto.AdminNotificationSummary;
import com.paceleague.notification.application.port.in.shared.AdminNotificationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notifications")
@Tag(name = "Admin - Notification", description = "관리자 알림관리 화면 API — 회원을 골라 푸시 알림 발송, 발송 내역 조회.")
@SecurityRequirement(name = "adminSession")
public class AdminNotificationController {

    private final AdminNotificationUseCase adminNotificationUseCase;

    public AdminNotificationController(AdminNotificationUseCase adminNotificationUseCase) {
        this.adminNotificationUseCase = adminNotificationUseCase;
    }

    @Operation(summary = "알림 목록", description = "최신 발송순 페이지네이션. targetNicknames 는 받는 회원 앞쪽 3명.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseApi<Page<AdminNotificationSummary>> list(
            @AdminSno Long adminSno,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseApi.success(adminNotificationUseCase.list(page, size));
    }

    @Operation(summary = "알림 상세", description = "본문 HTML + 받는 회원별 발송 여부.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{notificationSno}")
    public ResponseApi<AdminNotificationDetail> get(@AdminSno Long adminSno, @PathVariable Long notificationSno) {
        return ResponseApi.success(adminNotificationUseCase.get(notificationSno));
    }

    @Operation(summary = "알림 등록 + 발송",
            description = "저장 즉시 고른 회원들에게 개인 푸시를 보낸다. 탈퇴/서비스 알림 거부/FCM 토큰 없음 회원은 건너뛴다(sent=false).")
    @ApiResponse(responseCode = "200", description = "발송 완료(결과는 sentCount / targets[].sent)")
    @PostMapping
    public ResponseApi<AdminNotificationDetail> send(@AdminSno Long adminSno, @RequestBody AdminNotificationSendRequest req) {
        return ResponseApi.success(adminNotificationUseCase.send(adminSno, req.title(), req.content(), req.memberSnos()));
    }
}

package com.paceleague.notification.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.MemberSno;
import com.paceleague.notification.application.dto.NotificationHistoryPage;
import com.paceleague.notification.application.port.in.GetNotificationHistoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림내역 API — 발송된 푸시 알림 목록. 로그인 필요.")
public class NotificationController {

    private final GetNotificationHistoryUseCase getNotificationHistoryUseCase;

    @Operation(summary = "내 알림내역",
            description = "최신순. 첫 요청은 cursor 없이, 이후 '더보기'는 응답의 nextCursor 를 cursor 로 넘긴다. "
                    + "hasMore=false 면 마지막 페이지. 전체 발송 알림은 가입 이후 것만 포함.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseApi<NotificationHistoryPage> getHistory(
            @MemberSno Long memberSno,
            @Parameter(description = "이전 응답의 nextCursor (첫 페이지는 생략)") @RequestParam(required = false) Long cursor,
            @Parameter(description = "한 번에 가져올 개수 (기본 5, 최대 50)") @RequestParam(defaultValue = "5") int size) {
        return ResponseApi.success(getNotificationHistoryUseCase.getHistory(memberSno, cursor, size));
    }
}

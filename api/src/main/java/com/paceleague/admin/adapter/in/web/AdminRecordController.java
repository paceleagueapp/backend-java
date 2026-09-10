package com.paceleague.admin.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import com.paceleague.record.application.dto.AdminRecordSummary;
import com.paceleague.record.application.port.in.shared.AdminRecordQueryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/records")
@Tag(name = "Admin - Record", description = "관리자 러닝데이터관리 화면 API")
@SecurityRequirement(name = "adminSession")
public class AdminRecordController {

    private final AdminRecordQueryPort adminRecordQueryPort;

    public AdminRecordController(AdminRecordQueryPort adminRecordQueryPort) {
        this.adminRecordQueryPort = adminRecordQueryPort;
    }

    @Operation(summary = "러닝 기록 목록", description = "전체 회원의 러닝 기록, 최신순 페이지네이션.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseApi<Page<AdminRecordSummary>> list(
            @AdminSno Long adminSno,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseApi.success(adminRecordQueryPort.listRecords(page, size));
    }
}

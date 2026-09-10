package com.paceleague.admin.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import com.paceleague.territory.application.dto.AdminTerritorySummary;
import com.paceleague.territory.application.port.in.shared.AdminTerritoryQueryPort;
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
@RequestMapping("/api/admin/territories")
@Tag(name = "Admin - Territory", description = "관리자 랜드잇데이터관리 화면 API")
@SecurityRequirement(name = "adminSession")
public class AdminTerritoryController {

    private final AdminTerritoryQueryPort adminTerritoryQueryPort;

    public AdminTerritoryController(AdminTerritoryQueryPort adminTerritoryQueryPort) {
        this.adminTerritoryQueryPort = adminTerritoryQueryPort;
    }

    @Operation(summary = "랜드잇 땅 목록", description = "전체 ACTIVE 땅, 최신순 페이지네이션.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseApi<Page<AdminTerritorySummary>> list(
            @AdminSno Long adminSno,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseApi.success(adminTerritoryQueryPort.listTerritories(page, size));
    }
}

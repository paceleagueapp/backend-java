package com.paceleague.admin.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import com.paceleague.ranking.application.dto.AdminRankingEntry;
import com.paceleague.ranking.application.port.in.shared.AdminRankingQueryPort;
import com.paceleague.territory.application.dto.AdminTerritoryRankingEntry;
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
@RequestMapping("/api/admin/ranking")
@Tag(name = "Admin - Ranking", description = "관리자 랭킹관리 화면 API — 기록랭킹/랜드잇랭킹 탭")
@SecurityRequirement(name = "adminSession")
public class AdminRankingController {

    private final AdminRankingQueryPort adminRankingQueryPort;
    private final AdminTerritoryQueryPort adminTerritoryQueryPort;

    public AdminRankingController(AdminRankingQueryPort adminRankingQueryPort,
                                   AdminTerritoryQueryPort adminTerritoryQueryPort) {
        this.adminRankingQueryPort = adminRankingQueryPort;
        this.adminTerritoryQueryPort = adminTerritoryQueryPort;
    }

    @Operation(summary = "기록랭킹 탭", description = "현재 시즌 전체 회원을 점수 내림차순으로 페이지네이션.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/records")
    public ResponseApi<Page<AdminRankingEntry>> records(
            @AdminSno Long adminSno,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseApi.success(adminRankingQueryPort.getRankingPage(page, size));
    }

    @Operation(summary = "랜드잇랭킹 탭", description = "소유자별 총 점령 면적 내림차순으로 페이지네이션(공개 API의 top-N 캡 없음).")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/territory")
    public ResponseApi<Page<AdminTerritoryRankingEntry>> territory(
            @AdminSno Long adminSno,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseApi.success(adminTerritoryQueryPort.getRankingPage(page, size));
    }
}

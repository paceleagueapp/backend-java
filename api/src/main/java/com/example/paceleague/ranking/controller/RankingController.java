package com.example.paceleague.ranking.controller;

import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.ranking.dto.RankingPageResponse;
import com.example.paceleague.ranking.dto.RankingUserResponse;
import com.example.paceleague.ranking.service.RankingQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
@Tag(name = "Ranking", description = "리더보드(랭킹) 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class RankingController {

    private final RankingQueryService rankingQueryService;

    @Operation(summary = "랭킹 페이지 조회", description = "시즌 Top3와 내 주변 순위 5명을 함께 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/getRanking")
    public ResponseApi<RankingPageResponse> getRanking(
            Authentication authentication
    ) {
        Long uno = uno(authentication);
        return ResponseApi.success(
                rankingQueryService.getRankingPage(uno)
        );
    }

    @Operation(summary = "상위 10명 랭킹 조회", description = "인증 불필요. paceleague.co.kr 랜딩 페이지에서 표시하는 시즌 상위 10명 랭킹입니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirements
    @GetMapping("/top10")
    public ResponseApi<List<RankingUserResponse>> getTop10() {
        return ResponseApi.success(rankingQueryService.getTop10());
    }

    private long uno(Authentication authentication) {
        var p = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();
        return p.memberSno();
    }
}
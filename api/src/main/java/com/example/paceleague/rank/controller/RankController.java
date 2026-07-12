package com.example.paceleague.rank.controller;

import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.member.service.JwtTokenProvider;
import com.example.paceleague.rank.dto.RankMeResponse;
import com.example.paceleague.rank.service.RankQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
@Tag(name = "Rank", description = "내 점수/티어 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class RankController {

    private final RankQueryService rankQueryService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "내 랭크 조회", description = "현재 시즌 기준 내 누적 점수, 현재 티어, 다음 티어까지 남은 점수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/me")
    public ResponseApi<RankMeResponse> getMyRank(Authentication authentication) {
        Long uno = uno(authentication);

        RankMeResponse response = rankQueryService.getMyRank(uno);

        return ResponseApi.success(response);
    }

    private long uno(Authentication authentication) {
        var p = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();
        return p.memberSno();
    }
}

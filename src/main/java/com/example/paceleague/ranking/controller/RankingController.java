package com.example.paceleague.ranking.controller;

import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.ranking.dto.RankingPageResponse;
import com.example.paceleague.ranking.service.RankingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingQueryService rankingQueryService;

    @GetMapping("/getRanking")
    public ResponseApi<RankingPageResponse> getRanking(
            Authentication authentication
    ) {
        Long uno = uno(authentication);
        return ResponseApi.success(
                rankingQueryService.getRankingPage(uno)
        );
    }

    private long uno(Authentication authentication) {
        var p = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();
        return p.memberSno();
    }
}
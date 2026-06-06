package com.example.paceleague.rank.controller;

import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.member.service.JwtTokenProvider;
import com.example.paceleague.rank.dto.RankMeResponse;
import com.example.paceleague.rank.service.RankQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
public class RankController {

    private final RankQueryService rankQueryService;
    private final JwtTokenProvider jwtTokenProvider;

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

package com.example.paceleague.rank.adapter.in.web;

import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.web.MemberSno;
import com.example.paceleague.rank.application.dto.RankMeResponse;
import com.example.paceleague.rank.application.port.in.GetMyRankUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
@Tag(name = "Rank", description = "내 점수/티어 조회 API")
@SecurityRequirement(name = "bearerAuth")
public class RankController {

    private final GetMyRankUseCase getMyRankUseCase;

    @Operation(summary = "내 랭크 조회", description = "현재 시즌 기준 내 누적 점수, 현재 티어, 다음 티어까지 남은 점수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/me")
    public ResponseApi<RankMeResponse> getMyRank(
            @MemberSno Long memberSno,
            @Parameter(description = "티어 라벨 표시 언어(ko/en/ja/zh/es/fr/de/pt/vi/th), 미지원 값이면 ko") @RequestParam(defaultValue = "ko") String lang
    ) {
        RankMeResponse response = getMyRankUseCase.getMyRank(memberSno, lang);

        return ResponseApi.success(response);
    }
}

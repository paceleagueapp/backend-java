package com.example.paceleague.territory.adapter.in.web;

import com.example.paceleague.common.i18n.LocaleResolver;
import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.web.MemberSno;
import com.example.paceleague.territory.application.dto.TerritoryMapQuery;
import com.example.paceleague.territory.application.dto.TerritoryMapResponse;
import com.example.paceleague.territory.application.port.in.GetTerritoryMapUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/territory")
@RequiredArgsConstructor
@Tag(name = "Territory", description = "러닝 땅따먹기 지도/영역 조회 API")
public class TerritoryController {

    private final GetTerritoryMapUseCase getTerritoryMapUseCase;

    @Operation(summary = "지도 영역 내 땅 조회",
            description = "인증 불필요. 지도가 보고 있는 bounds(남서/북동 위경도)와 줌 레벨로 점령된 땅 목록을 반환합니다. "
                    + "줌이 임계값 미만이면 빈 목록 + zoomTooLow=true 를 돌려줍니다(데이터 과다 방지). "
                    + "로그인 상태면 각 땅의 mine 플래그가 채워집니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirements
    @GetMapping("/map")
    public ResponseApi<TerritoryMapResponse> getMap(
            @MemberSno(required = false) Long memberSno,
            @Parameter(description = "남서쪽 위도") @RequestParam double swLat,
            @Parameter(description = "남서쪽 경도") @RequestParam double swLng,
            @Parameter(description = "북동쪽 위도") @RequestParam double neLat,
            @Parameter(description = "북동쪽 경도") @RequestParam double neLng,
            @Parameter(description = "지도 줌 레벨") @RequestParam int zoom,
            @Parameter(description = "티어 라벨 언어(ko/en/ja/zh/es/fr/de/pt/vi/th), 미지원 값이면 ko")
            @RequestParam(defaultValue = "ko") String lang,
            @Parameter(description = "ISO 3166-1 alpha-2 국가코드(예: KR). 주어지면 lang 대신 이 국가에 맞는 언어로 응답")
            @RequestParam(required = false) String country
    ) {
        TerritoryMapQuery query = new TerritoryMapQuery(
                swLat, swLng, neLat, neLng, zoom,
                LocaleResolver.resolve(lang, country).toCode(),
                memberSno);
        return ResponseApi.success(getTerritoryMapUseCase.getMap(query));
    }
}

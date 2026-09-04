package com.paceleague.common.web;

import com.paceleague.common.i18n.CountryLanguageResolver;
import com.paceleague.common.i18n.Language;
import com.paceleague.common.i18n.LanguageResponse;
import com.paceleague.common.response.ResponseApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Locale", description = "국가/언어 판별 유틸리티 API")
public class LocaleController {

    @Operation(
            summary = "국가 코드에 맞는 언어 조회",
            description = "ISO 3166-1 alpha-2 국가코드(예: KR, US, JP)를 받아 board/rank/ranking API의 lang 파라미터로 그대로 쓸 수 있는 언어 코드를 반환합니다. " +
                    "지원 10개 언어(ko/en/ja/zh/es/fr/de/pt/vi/th) 중 하나이며, 매핑에 없는 국가나 값 미전달 시 en을 반환합니다. 공개(인증 불필요)."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @SecurityRequirements
    @GetMapping("/api/common/language")
    public ResponseApi<LanguageResponse> getLanguageByCountry(
            @Parameter(description = "ISO 3166-1 alpha-2 국가코드 (예: KR, US, JP)") @RequestParam(required = false) String country
    ) {
        Language language = CountryLanguageResolver.resolve(country);
        return ResponseApi.success(LanguageResponse.of(language));
    }
}

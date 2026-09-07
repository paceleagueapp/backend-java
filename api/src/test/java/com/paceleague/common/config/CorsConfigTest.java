package com.paceleague.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 운영 프로필(!local)의 CORS 설정을 CorsConfiguration 레벨에서 직접 검증한다 — Spring 컨텍스트/MockMvc 없이
// 순수 단위 테스트(다른 테스트들과 같은 방식). 2026-09-07 회귀 방지: /api/territory/map·ranking이
// readOnlyPublic(헤더 미허용)을 쓰던 시절엔 로그인 사용자의 Authorization 프리플라이트가 403으로 막혀
// 지도/랭킹이 아예 안 떴다.
class CorsConfigTest {

    private final CorsConfigurationSource source = new CorsConfig().corsConfigurationSource();

    private CorsConfiguration configFor(String method, String path) {
        return source.getCorsConfiguration(new MockHttpServletRequest(method, path));
    }

    @Test
    void territory_지도조회는_로그인_사용자의_Authorization_프리플라이트를_허용한다() {
        CorsConfiguration config = configFor("OPTIONS", "/api/territory/map");

        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("https://paceleague.co.kr")).isEqualTo("https://paceleague.co.kr");
        assertThat(config.checkHttpMethod(HttpMethod.GET)).isNotNull();
        // 회귀 지점: allowedHeaders 미설정이면 여기가 null → DefaultCorsProcessor가 프리플라이트를 403으로 거부.
        assertThat(config.checkHeaders(List.of("authorization"))).contains("authorization");
    }

    @Test
    void territory_면적랭킹도_동일하게_Authorization_헤더를_허용한다() {
        CorsConfiguration config = configFor("OPTIONS", "/api/territory/ranking");

        assertThat(config).isNotNull();
        assertThat(config.checkHeaders(List.of("authorization"))).contains("authorization");
    }

    @Test
    void territory_엔드포인트는_GET만_허용하고_쓰기_메서드는_막는다() {
        CorsConfiguration config = configFor("OPTIONS", "/api/territory/map");

        assertThat(config.checkHttpMethod(HttpMethod.POST)).isNull();
        assertThat(config.checkHttpMethod(HttpMethod.PUT)).isNull();
        assertThat(config.checkHttpMethod(HttpMethod.DELETE)).isNull();
    }

    @Test
    void 허용되지_않은_오리진은_거부된다() {
        CorsConfiguration config = configFor("GET", "/api/territory/map");

        assertThat(config.checkOrigin("https://evil.example.com")).isNull();
        assertThat(config.checkOrigin("http://paceleague.co.kr")).isNull(); // http 다운그레이드도 불가
    }

    @Test
    void CORS가_등록되지_않은_경로는_설정이_없다() {
        assertThat(configFor("GET", "/api/rank/me")).isNull();
        assertThat(configFor("GET", "/api/record/gps")).isNull();
    }

    @Test
    void 최근30일_기록조회는_기존과_동일하게_GET과_Authorization만_허용한다() {
        CorsConfiguration config = configFor("OPTIONS", "/api/record/recent-30-days");

        assertThat(config).isNotNull();
        assertThat(config.checkHttpMethod(HttpMethod.GET)).isNotNull();
        assertThat(config.checkHttpMethod(HttpMethod.POST)).isNull();
        assertThat(config.checkHeaders(List.of("authorization"))).contains("authorization");
    }
}

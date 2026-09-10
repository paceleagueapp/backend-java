package com.paceleague.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    @Profile("!local")
    public CorsConfigurationSource corsConfigurationSource() {
        return buildSource(List.of("https://paceleague.co.kr", "https://www.paceleague.co.kr"));
    }

    // 로컬 개발 편의를 위해 localhost origin도 함께 허용한다. 운영 설정에는 영향 없음.
    @Bean
    @Profile("local")
    public CorsConfigurationSource localCorsConfigurationSource() {
        return buildSource(List.of(
                "https://paceleague.co.kr", "https://www.paceleague.co.kr",
                "http://localhost:*", "http://127.0.0.1:*"
        ));
    }

    private CorsConfigurationSource buildSource(List<String> origins) {
        CorsConfiguration readOnlyPublic = new CorsConfiguration();
        readOnlyPublic.setAllowedOriginPatterns(origins);
        readOnlyPublic.setAllowedMethods(List.of("GET"));

        CorsConfiguration authAndBoard = new CorsConfiguration();
        authAndBoard.setAllowedOriginPatterns(origins);
        // PUT은 게시글 수정(PUT /api/board/posts/{postSno})을 위해 추가됨 — 이후 회원 동의 갱신
        // (PUT /api/member/agreements)에도 쓰이게 됨. media에는 여전히 PUT 엔드포인트가 없지만
        // 세 경로가 같은 CorsConfiguration을 공유하므로 여기서 함께 허용.
        authAndBoard.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        authAndBoard.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // GET 전용이지만 브라우저가 Authorization 헤더를 실어 보내는(=preflight가 뜨는) 공개/개인화 조회들.
        //  - /api/record/recent-30-days: 게시글 작성 화면의 "내 러닝기록 첨부" (로그인 필수)
        //  - /api/territory/map, /api/territory/ranking: 비로그인도 되지만, 로그인 시 토큰으로 mine 플래그를
        //    채우려면 web/js/app.js의 apiFetch가 Authorization을 붙인다 → 이 헤더를 허용하지 않으면
        //    로그인 사용자의 preflight가 403으로 막혀 지도/랭킹이 아예 안 뜬다.
        // 이 헤더 허용이 없으면 비로그인만 동작하고 로그인 시 CORS 오류가 난다.
        CorsConfiguration getWithAuthHeader = new CorsConfiguration();
        getWithAuthHeader.setAllowedOriginPatterns(origins);
        getWithAuthHeader.setAllowedMethods(List.of("GET"));
        getWithAuthHeader.setAllowedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/ranking/top10", readOnlyPublic);
        source.registerCorsConfiguration("/api/member/**", authAndBoard);
        source.registerCorsConfiguration("/api/board/**", authAndBoard);
        source.registerCorsConfiguration("/api/record/recent-30-days", getWithAuthHeader);
        // 게시글 작성 화면의 이미지/동영상/링크 첨부(presigned URL 발급/완료/폴링/링크 생성) — 이 API 자체는
        // 파일 바이트를 다루지 않고 메타데이터/URL만 오간다. 실제 파일 PUT은 브라우저가 S3에 직접 하며,
        // 그건 이 Spring CORS와 무관한 S3 버킷 자체의 CORS 설정(docs/infra.md)이 담당한다.
        source.registerCorsConfiguration("/api/media/**", authAndBoard);
        // 땅따먹기 지도/랭킹 페이지(web/territory.html)가 브라우저에서 호출하는 공개 조회 — GET만.
        // 비로그인도 되지만 로그인 시 apiFetch가 Authorization을 붙이므로 getWithAuthHeader를 써야
        // preflight가 통과한다(readOnlyPublic이면 로그인 사용자에게 CORS 403).
        source.registerCorsConfiguration("/api/territory/map", getWithAuthHeader);
        source.registerCorsConfiguration("/api/territory/ranking", getWithAuthHeader);
        // 크루 페이지(web/crew.html)가 호출하는 크루 API — board와 같은 shape(GET/POST/PUT/DELETE + 헤더).
        source.registerCorsConfiguration("/api/crew/**", authAndBoard);

        // 관리자 페이지(web/admin/**) — Authorization이 아니라 X-Admin-Session 헤더를 쓴다(AdminSessionFilter).
        // 회원관리/랭킹관리 등에서 조만간 PUT/DELETE도 쓸 게 뻔해서 board와 같은 메서드 셋을 미리 허용.
        CorsConfiguration adminConfig = new CorsConfiguration();
        adminConfig.setAllowedOriginPatterns(origins);
        adminConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        adminConfig.setAllowedHeaders(List.of("X-Admin-Session", "Content-Type"));
        source.registerCorsConfiguration("/api/admin/**", adminConfig);

        return source;
    }
}

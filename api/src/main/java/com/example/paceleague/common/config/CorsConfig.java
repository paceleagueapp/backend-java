package com.example.paceleague.common.config;

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
        // PUT은 게시글 수정(PUT /api/board/posts/{postSno})을 위해 추가됨 — member/media에는 PUT 엔드포인트가
        // 없어 실질적으로는 board에만 해당하지만, 세 경로가 같은 CorsConfiguration을 공유하므로 여기서 함께 허용.
        authAndBoard.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        authAndBoard.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // 게시글 작성 화면에서 "내 러닝기록 첨부"를 위해 최근 30일 기록만 조회할 수 있도록 예외적으로 연다.
        // record 도메인의 나머지 엔드포인트는 여전히 CORS 미허용.
        CorsConfiguration recordSelectForBoard = new CorsConfiguration();
        recordSelectForBoard.setAllowedOriginPatterns(origins);
        recordSelectForBoard.setAllowedMethods(List.of("GET"));
        recordSelectForBoard.setAllowedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/ranking/top10", readOnlyPublic);
        source.registerCorsConfiguration("/api/member/**", authAndBoard);
        source.registerCorsConfiguration("/api/board/**", authAndBoard);
        source.registerCorsConfiguration("/api/record/recent-30-days", recordSelectForBoard);
        // 게시글 작성 화면의 이미지/동영상/링크 첨부(presigned URL 발급/완료/폴링/링크 생성) — 이 API 자체는
        // 파일 바이트를 다루지 않고 메타데이터/URL만 오간다. 실제 파일 PUT은 브라우저가 S3에 직접 하며,
        // 그건 이 Spring CORS와 무관한 S3 버킷 자체의 CORS 설정(docs/infra.md)이 담당한다.
        source.registerCorsConfiguration("/api/media/**", authAndBoard);
        // 땅따먹기 지도 페이지(web/territory.html)가 브라우저에서 호출하는 공개 조회 — GET만.
        source.registerCorsConfiguration("/api/territory/map", readOnlyPublic);
        // 크루 페이지(web/crew.html)가 호출하는 크루 API — board와 같은 shape(GET/POST/PUT/DELETE + 헤더).
        source.registerCorsConfiguration("/api/crew/**", authAndBoard);
        return source;
    }
}

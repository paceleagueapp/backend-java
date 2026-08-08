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
        authAndBoard.setAllowedMethods(List.of("GET", "POST", "DELETE"));
        authAndBoard.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/ranking/top10", readOnlyPublic);
        source.registerCorsConfiguration("/api/member/**", authAndBoard);
        source.registerCorsConfiguration("/api/board/**", authAndBoard);
        return source;
    }
}

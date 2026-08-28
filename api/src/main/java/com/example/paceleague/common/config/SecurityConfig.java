package com.example.paceleague.common.config;

import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.common.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/member/join",
                                "/api/member/login",
                                "/api/member/reissue",
                                "/api/member/logout",
                                // Swagger/OpenAPI 경로 — 운영(prod)에서는 springdoc 자체를 꺼서 404이므로 실질적으로 로컬 전용.
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api/app/version-check",
                                "/api/ranking/top10",
                                "/api/common/language",
                                "/robots.txt"
                        ).permitAll()
                        // 게시판 조회(GET)는 레딧처럼 비로그인도 가능 — 작성/삭제/추천(POST/DELETE)은 아래 anyRequest()에 걸려 로그인 필요.
                        .requestMatchers(HttpMethod.GET,
                                "/api/board",
                                "/api/board/*/posts",
                                "/api/board/posts/*",
                                "/api/board/posts/*/comments"
                        ).permitAll()
                        // 땅따먹기 지도는 비로그인도 볼 수 있다(로그인 시 mine 플래그만 추가로 채워짐).
                        .requestMatchers(HttpMethod.GET, "/api/territory/map").permitAll()
                        // 크루 검색은 비로그인도 가능(크루 상세/내 크루 등 나머지는 로그인 필요).
                        .requestMatchers(HttpMethod.GET, "/api/crew/search").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                            {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                        """);
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                            {"success":false,"code":"FORBIDDEN","message":"접근 권한이 없습니다."}
                        """);
                        })
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

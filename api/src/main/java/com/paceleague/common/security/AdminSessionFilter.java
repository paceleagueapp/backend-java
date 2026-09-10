package com.paceleague.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

// member의 JwtAuthenticationFilter와 같은 위치(공통 인증 primitive)지만, admin은 JWT가 아니라
// 서버 세션(Redis, 로그아웃 시 즉시 무효화 가능)이라 검증 방식이 다르다 — 토큰 자체가 아니라
// Redis에 저장된 세션의 존재 여부로 인증 여부를 판단한다.
public class AdminSessionFilter extends OncePerRequestFilter {

    public static final String SESSION_HEADER = "X-Admin-Session";
    public static final String REDIS_KEY_PREFIX = "admin:session:";
    public static final Duration SESSION_TTL = Duration.ofHours(12);

    private final StringRedisTemplate redis;

    public AdminSessionFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = request.getHeader(SESSION_HEADER);

        if (token != null && !token.isBlank()) {
            String key = REDIS_KEY_PREFIX + token;
            String value = redis.opsForValue().get(key);

            if (value != null) {
                // 활동 중인 세션은 만료를 밀어준다(sliding session) — 작업 중에 12시간 고정 만료로 갑자기 끊기지 않게.
                redis.expire(key, SESSION_TTL);

                var principal = new AdminPrincipal(Long.parseLong(value));
                var auth = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    public record AdminPrincipal(long adminSno) {}
}

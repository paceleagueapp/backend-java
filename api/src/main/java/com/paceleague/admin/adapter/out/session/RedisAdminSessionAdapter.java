package com.paceleague.admin.adapter.out.session;

import com.paceleague.admin.application.port.out.AdminSessionStorePort;
import com.paceleague.common.security.AdminSessionFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// member의 RedisRefreshTokenAdapter와 같은 패턴(랜덤 토큰 + Redis TTL) — JWT가 아니라 서버 세션이라
// 로그아웃 시 즉시 무효화할 수 있다. 실제 요청별 검증/슬라이딩 만료는 common.security.AdminSessionFilter가
// 같은 키 프리픽스로 직접 수행한다(검증은 순수 조회라 별도 포트로 감싸지 않음 — JwtAuthenticationFilter가
// JwtTokenProvider를 직접 쓰는 것과 같은 이유).
@Component
public class RedisAdminSessionAdapter implements AdminSessionStorePort {

    private final StringRedisTemplate redis;

    public RedisAdminSessionAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public String issue(Long adminSno) {
        String token = RandomStringUtils.randomAlphanumeric(64);
        redis.opsForValue().set(
                AdminSessionFilter.REDIS_KEY_PREFIX + token,
                String.valueOf(adminSno),
                AdminSessionFilter.SESSION_TTL
        );
        return token;
    }

    @Override
    public void revoke(String sessionToken) {
        redis.delete(AdminSessionFilter.REDIS_KEY_PREFIX + sessionToken);
    }
}

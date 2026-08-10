package com.example.paceleague.member.adapter.out.token;

import com.example.paceleague.common.config.JwtProperties;
import com.example.paceleague.member.application.port.out.RefreshTokenStorePort;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRefreshTokenAdapter implements RefreshTokenStorePort {
    private final StringRedisTemplate redis;
    private final JwtProperties props;

    public RedisRefreshTokenAdapter(StringRedisTemplate redis, JwtProperties props) {
        this.redis = redis;
        this.props = props;
    }

    public String issue(long memberSno) {
        // refresh token은 JWT로 굳이 안 만들어도 됨(랜덤 토큰 + Redis TTL)
        String token = RandomStringUtils.randomAlphanumeric(64);

        String key = "refresh:" + token;
        redis.opsForValue().set(key, String.valueOf(memberSno), Duration.ofSeconds(props.refreshTokenTtlSeconds()));

        return token;
    }

    public void revoke(String refreshToken) {
        redis.delete("refresh:" + refreshToken);
    }

    // GETDEL로 조회+삭제를 원자적으로 묶어, 동시에 들어온 두 reissue 요청이 같은 refresh token으로 둘 다 통과하는 걸 막는다.
    public Long validateAndRevoke(String refreshToken) {
        String key = "refresh:" + refreshToken;
        String value = redis.opsForValue().getAndDelete(key);
        if (value == null) {
            throw new IllegalArgumentException("refresh token expired or invalid");
        }
        return Long.parseLong(value);
    }
}

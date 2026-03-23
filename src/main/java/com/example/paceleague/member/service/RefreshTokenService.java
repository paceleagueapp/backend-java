package com.example.paceleague.member.service;

import com.example.paceleague.common.config.JwtProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenService {
    private final StringRedisTemplate redis;
    private final JwtProperties props;

    public RefreshTokenService(StringRedisTemplate redis, JwtProperties props) {
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

    public Long getMemberSnoByRefreshToken(String refreshToken) {
        String key = "refresh:" + refreshToken;
        String value = redis.opsForValue().get(key);
        if (value == null) return null;
        return Long.parseLong(value);
    }

    public void revoke(String refreshToken) {
        redis.delete("refresh:" + refreshToken);
    }

    public Long validate(String refreshToken) {
        Long memberSno = getMemberSnoByRefreshToken(refreshToken);
        if (memberSno == null) {
            throw new IllegalArgumentException("refresh token expired or invalid");
        }
        return memberSno;
    }
}

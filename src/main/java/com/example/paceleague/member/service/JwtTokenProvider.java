package com.example.paceleague.member.service;

import com.example.paceleague.common.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtTokenProvider {
    private final JwtProperties props;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(long memberSno, String memberId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.accessTtlSeconds());

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(memberSno))
                .claim("memberId", memberId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }
}

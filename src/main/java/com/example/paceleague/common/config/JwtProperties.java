package com.example.paceleague.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties (
    String issuer,
    String secret,
    long accessTokenTtlSeconds,
    long refreshTokenTtlSeconds
){}

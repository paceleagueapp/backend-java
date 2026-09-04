package com.paceleague.member.application.dto;

public record TokenResponse (
        String tokenType,   // "Bearer"
        String accessToken,
        long accessExpiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds,
        String nickname
){}

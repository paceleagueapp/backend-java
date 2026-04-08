package com.example.paceleague.member.dto;

public record AuthTokenInfo(
        String grantType,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {
}
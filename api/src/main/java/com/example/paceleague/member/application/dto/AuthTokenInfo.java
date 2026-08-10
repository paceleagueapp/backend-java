package com.example.paceleague.member.application.dto;

public record AuthTokenInfo(
        String grantType,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn,
        String nickname
) {
}

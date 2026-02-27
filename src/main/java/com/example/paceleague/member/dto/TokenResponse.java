package com.example.paceleague.member.dto;

public record TokenResponse (
        String tokenType,   // "Bearer"
        String accessToken,
        long accessExpiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds
){}

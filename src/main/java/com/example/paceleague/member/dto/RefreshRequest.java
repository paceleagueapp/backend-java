package com.example.paceleague.member.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest (
        @NotBlank String refreshToken
){}

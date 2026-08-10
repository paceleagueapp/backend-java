package com.example.paceleague.member.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
        @NotBlank String memberId,
        @NotBlank String password
){}

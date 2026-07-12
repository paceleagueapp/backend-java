package com.example.paceleague.member.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
        @NotBlank String memberId,
        @NotBlank String password
){}

package com.paceleague.admin.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank String adminId,
        @NotBlank String password
) {}

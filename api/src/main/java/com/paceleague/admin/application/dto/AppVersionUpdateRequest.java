package com.paceleague.admin.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AppVersionUpdateRequest(
        @NotBlank String latestVersion,
        @NotBlank String minRequiredVersion
) {}

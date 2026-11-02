package com.paceleague.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CouponImageUploadRequest(
        @NotBlank String mimeType,
        @NotNull Long fileSizeBytes
) {}

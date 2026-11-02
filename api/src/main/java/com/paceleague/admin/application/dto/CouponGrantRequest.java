package com.paceleague.admin.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// expiresAt: 이 날짜(KST)까지 사용 가능, "yyyy-MM-dd".
public record CouponGrantRequest(
        @NotNull Long memberSno,
        @NotBlank String name,
        @NotNull LocalDate expiresAt,
        @NotBlank String imageKey
) {}

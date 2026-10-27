package com.paceleague.notification.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// platform 은 등록(POST) 때만 필요 — ANDROID / IOS. 해제(DELETE)는 token 만.
public record DeviceTokenRequest(
        @NotBlank @Size(max = 255) String token,
        String platform
) {}

package com.paceleague.admin.application.dto;

public record AdminSessionInfo(
        String sessionToken,
        String adminId
) {}

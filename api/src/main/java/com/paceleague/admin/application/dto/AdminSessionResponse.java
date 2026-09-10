package com.paceleague.admin.application.dto;

public record AdminSessionResponse(
        String sessionToken,
        String adminId
) {}

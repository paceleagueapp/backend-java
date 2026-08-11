package com.example.paceleague.media.application.dto;

public record MediaUploadInitResponse(
        Long mediaSno,
        String uploadUrl,
        long expiresInSeconds
) {
}

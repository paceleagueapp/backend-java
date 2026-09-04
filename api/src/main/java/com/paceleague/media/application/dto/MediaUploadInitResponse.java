package com.paceleague.media.application.dto;

public record MediaUploadInitResponse(
        Long mediaSno,
        String uploadUrl,
        long expiresInSeconds
) {
}

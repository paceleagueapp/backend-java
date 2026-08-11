package com.example.paceleague.media.application.dto;

import com.example.paceleague.media.domain.enums.MediaType;

public record MediaUploadRequest(
        MediaType type,
        String mimeType,
        Long fileSizeBytes
) {
}

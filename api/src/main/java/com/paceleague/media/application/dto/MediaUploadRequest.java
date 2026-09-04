package com.paceleague.media.application.dto;

import com.paceleague.media.domain.enums.MediaType;

public record MediaUploadRequest(
        MediaType type,
        String mimeType,
        Long fileSizeBytes
) {
}

package com.paceleague.media.application.dto;

import com.paceleague.media.domain.entity.Media;
import com.paceleague.media.domain.enums.MediaType;

public record MediaAttachmentResponse(
        Long mediaSno,
        MediaType type,
        String url
) {
    public static MediaAttachmentResponse from(Media media) {
        return new MediaAttachmentResponse(media.getSno(), media.getType(), media.getUrl());
    }
}

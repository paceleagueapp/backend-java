package com.paceleague.media.application.dto;

import com.paceleague.media.domain.entity.Media;
import com.paceleague.media.domain.enums.MediaStatus;

public record MediaStatusResponse(
        Long mediaSno,
        MediaStatus status,
        String url,
        String moderationReason
) {
    public static MediaStatusResponse from(Media media) {
        return new MediaStatusResponse(
                media.getSno(),
                media.getStatus(),
                media.getUrl(),
                media.getModerationReason()
        );
    }
}

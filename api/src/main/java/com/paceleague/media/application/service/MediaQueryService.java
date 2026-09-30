package com.paceleague.media.application.service;

import com.paceleague.media.application.dto.MediaAttachmentResponse;
import com.paceleague.media.application.port.in.shared.GetApprovedMediaUrlPort;
import com.paceleague.media.application.port.in.shared.GetPostAttachmentsPort;
import com.paceleague.media.application.port.out.MediaRepositoryPort;
import com.paceleague.media.domain.entity.Media;
import com.paceleague.media.domain.enums.MediaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaQueryService implements GetPostAttachmentsPort, GetApprovedMediaUrlPort {

    private final MediaRepositoryPort mediaRepositoryPort;

    public List<MediaAttachmentResponse> getByPostSno(Long postSno) {
        return mediaRepositoryPort.findByPostSno(postSno)
                .stream().map(MediaAttachmentResponse::from).toList();
    }

    public long countByPostSno(Long postSno) {
        return mediaRepositoryPort.countByPostSno(postSno);
    }

    public String requireApprovedUrl(Long mediaSno, Long ownerMemberSno) {
        Media media = mediaRepositoryPort.findBySnoAndMemberSno(mediaSno, ownerMemberSno)
                .orElseThrow(() -> new IllegalArgumentException("Image not found."));
        if (media.getStatus() != MediaStatus.APPROVED || media.getUrl() == null) {
            throw new IllegalArgumentException("This image has not been approved yet.");
        }
        return media.getUrl();
    }
}

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
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다"));
        if (media.getStatus() != MediaStatus.APPROVED || media.getUrl() == null) {
            throw new IllegalArgumentException("아직 승인되지 않은 이미지입니다");
        }
        return media.getUrl();
    }
}

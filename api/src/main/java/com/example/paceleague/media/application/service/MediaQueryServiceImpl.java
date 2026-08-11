package com.example.paceleague.media.application.service;

import com.example.paceleague.media.application.dto.MediaAttachmentResponse;
import com.example.paceleague.media.application.port.in.GetPostAttachmentsPort;
import com.example.paceleague.media.application.port.out.MediaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaQueryServiceImpl implements GetPostAttachmentsPort {

    private final MediaRepositoryPort mediaRepositoryPort;

    public List<MediaAttachmentResponse> getByPostSno(Long postSno) {
        return mediaRepositoryPort.findByPostSno(postSno)
                .stream().map(MediaAttachmentResponse::from).toList();
    }

    public long countByPostSno(Long postSno) {
        return mediaRepositoryPort.countByPostSno(postSno);
    }
}

package com.paceleague.media.adapter.out.persistence;

import com.paceleague.media.application.port.out.MediaRepositoryPort;
import com.paceleague.media.domain.entity.Media;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MediaPersistenceAdapter implements MediaRepositoryPort {

    private final MediaJpaRepository mediaJpaRepository;

    public Media save(Media media) {
        return mediaJpaRepository.save(media);
    }

    public Optional<Media> findById(Long sno) {
        return mediaJpaRepository.findById(sno);
    }

    public Optional<Media> findBySnoAndMemberSno(Long sno, Long memberSno) {
        return mediaJpaRepository.findBySnoAndMemberSno(sno, memberSno);
    }

    public List<Media> findByPostSno(Long postSno) {
        return mediaJpaRepository.findByPostSno(postSno);
    }

    public long countByPostSno(Long postSno) {
        return mediaJpaRepository.countByPostSno(postSno);
    }
}

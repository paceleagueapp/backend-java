package com.example.paceleague.media.application.port.out;

import com.example.paceleague.media.domain.entity.Media;

import java.util.List;
import java.util.Optional;

public interface MediaRepositoryPort {
    Media save(Media media);

    Optional<Media> findById(Long sno);

    Optional<Media> findBySnoAndMemberSno(Long sno, Long memberSno);

    List<Media> findByPostSno(Long postSno);

    long countByPostSno(Long postSno);
}

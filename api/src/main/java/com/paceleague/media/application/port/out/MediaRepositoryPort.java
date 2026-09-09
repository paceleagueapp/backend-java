package com.paceleague.media.application.port.out;

import com.paceleague.media.domain.entity.Media;

import java.util.List;
import java.util.Optional;

public interface MediaRepositoryPort {
    Media save(Media media);

    Optional<Media> findById(Long sno);

    Optional<Media> findBySnoAndMemberSno(Long sno, Long memberSno);

    List<Media> findByPostSno(Long postSno);

    long countByPostSno(Long postSno);

    // 회원 탈퇴 시 업로드 미디어 행 삭제.
    void deleteAllByMemberSno(Long memberSno);
}

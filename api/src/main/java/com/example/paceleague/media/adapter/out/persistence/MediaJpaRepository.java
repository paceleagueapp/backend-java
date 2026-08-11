package com.example.paceleague.media.adapter.out.persistence;

import com.example.paceleague.media.domain.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaJpaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findBySnoAndMemberSno(Long sno, Long memberSno);

    List<Media> findByPostSno(Long postSno);

    long countByPostSno(Long postSno);
}

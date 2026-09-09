package com.paceleague.media.adapter.out.persistence;

import com.paceleague.media.domain.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MediaJpaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findBySnoAndMemberSno(Long sno, Long memberSno);

    List<Media> findByPostSno(Long postSno);

    long countByPostSno(Long postSno);

    // 회원 탈퇴 시 업로드 미디어 행 삭제 (S3 객체는 best-effort, 후순위).
    @Modifying
    @Query("delete from Media m where m.memberSno = :memberSno")
    int deleteAllByMemberSno(@Param("memberSno") Long memberSno);
}

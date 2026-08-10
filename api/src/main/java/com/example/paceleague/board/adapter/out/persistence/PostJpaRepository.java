package com.example.paceleague.board.adapter.out.persistence;

import com.example.paceleague.board.domain.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostJpaRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardSno(Long boardSno, Pageable pageable);

    Optional<Post> findBySnoAndMemberSno(Long sno, Long memberSno);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.sno = :sno")
    Optional<Post> findBySnoForUpdate(@Param("sno") Long sno);

    @Modifying
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.sno = :sno")
    void incrementViewCount(@Param("sno") Long sno);
}

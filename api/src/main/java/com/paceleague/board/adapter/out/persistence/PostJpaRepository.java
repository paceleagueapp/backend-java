package com.paceleague.board.adapter.out.persistence;

import com.paceleague.board.domain.entity.Post;
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

    // 목록용 — 숨김(신고 누적) 글과 차단한 작성자의 글을 제외. blockedSnos 는 비어 있으면 안 되므로
    // (JPQL in () 불가) 호출부가 항상 최소 한 개(예: -1L)를 넣어 보낸다.
    @Query("select p from Post p where p.boardSno = :boardSno and p.hidden = false and p.memberSno not in :blockedSnos")
    Page<Post> findVisibleByBoardSno(@Param("boardSno") Long boardSno,
                                     @Param("blockedSnos") java.util.Collection<Long> blockedSnos,
                                     Pageable pageable);

    Optional<Post> findBySnoAndMemberSno(Long sno, Long memberSno);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.sno = :sno")
    Optional<Post> findBySnoForUpdate(@Param("sno") Long sno);

    @Modifying
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.sno = :sno")
    void incrementViewCount(@Param("sno") Long sno);
}

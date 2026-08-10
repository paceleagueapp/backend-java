package com.example.paceleague.board.adapter.out.persistence;

import com.example.paceleague.board.domain.entity.Comment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostSnoOrderByCreateAtAsc(Long postSno);

    Optional<Comment> findBySnoAndMemberSno(Long sno, Long memberSno);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comment c where c.sno = :sno")
    Optional<Comment> findBySnoForUpdate(@Param("sno") Long sno);

    long countByPostSno(Long postSno);

    List<Comment> findByParentCommentSno(Long parentCommentSno);

    void deleteByParentCommentSno(Long parentCommentSno);

    void deleteByPostSno(Long postSno);
}

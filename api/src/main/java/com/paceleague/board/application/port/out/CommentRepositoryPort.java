package com.paceleague.board.application.port.out;

import com.paceleague.board.domain.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepositoryPort {
    Comment save(Comment comment);

    Optional<Comment> findById(Long sno);

    List<Comment> findByPostSnoOrderByCreateAtAsc(Long postSno);

    Optional<Comment> findBySnoAndMemberSno(Long sno, Long memberSno);

    // @Lock(PESSIMISTIC_WRITE) — 어댑터 구현에서 그대로 보존
    Optional<Comment> findBySnoForUpdate(Long sno);

    long countByPostSno(Long postSno);

    List<Comment> findByParentCommentSno(Long parentCommentSno);

    void deleteByParentCommentSno(Long parentCommentSno);

    void deleteByPostSno(Long postSno);

    void delete(Comment comment);
}

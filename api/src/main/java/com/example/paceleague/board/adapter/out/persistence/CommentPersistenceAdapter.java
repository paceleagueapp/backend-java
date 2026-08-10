package com.example.paceleague.board.adapter.out.persistence;

import com.example.paceleague.board.application.port.out.CommentRepositoryPort;
import com.example.paceleague.board.domain.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentRepositoryPort {

    private final CommentJpaRepository commentJpaRepository;

    public Comment save(Comment comment) {
        return commentJpaRepository.save(comment);
    }

    public Optional<Comment> findById(Long sno) {
        return commentJpaRepository.findById(sno);
    }

    public List<Comment> findByPostSnoOrderByCreateAtAsc(Long postSno) {
        return commentJpaRepository.findByPostSnoOrderByCreateAtAsc(postSno);
    }

    public Optional<Comment> findBySnoAndMemberSno(Long sno, Long memberSno) {
        return commentJpaRepository.findBySnoAndMemberSno(sno, memberSno);
    }

    public Optional<Comment> findBySnoForUpdate(Long sno) {
        return commentJpaRepository.findBySnoForUpdate(sno);
    }

    public long countByPostSno(Long postSno) {
        return commentJpaRepository.countByPostSno(postSno);
    }

    public List<Comment> findByParentCommentSno(Long parentCommentSno) {
        return commentJpaRepository.findByParentCommentSno(parentCommentSno);
    }

    public void deleteByParentCommentSno(Long parentCommentSno) {
        commentJpaRepository.deleteByParentCommentSno(parentCommentSno);
    }

    public void deleteByPostSno(Long postSno) {
        commentJpaRepository.deleteByPostSno(postSno);
    }

    public void delete(Comment comment) {
        commentJpaRepository.delete(comment);
    }
}

package com.example.paceleague.board.repository;

import com.example.paceleague.board.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByCommentSnoAndMemberSno(Long commentSno, Long memberSno);

    void deleteByCommentSnoIn(List<Long> commentSnos);
}

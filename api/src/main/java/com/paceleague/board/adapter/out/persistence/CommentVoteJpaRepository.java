package com.paceleague.board.adapter.out.persistence;

import com.paceleague.board.domain.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentVoteJpaRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByCommentSnoAndMemberSno(Long commentSno, Long memberSno);

    void deleteByCommentSnoIn(List<Long> commentSnos);
}

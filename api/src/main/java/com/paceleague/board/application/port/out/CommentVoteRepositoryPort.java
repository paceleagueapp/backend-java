package com.paceleague.board.application.port.out;

import com.paceleague.board.domain.entity.CommentVote;

import java.util.List;
import java.util.Optional;

public interface CommentVoteRepositoryPort {
    CommentVote save(CommentVote vote);

    Optional<CommentVote> findByCommentSnoAndMemberSno(Long commentSno, Long memberSno);

    void deleteByCommentSnoIn(List<Long> commentSnos);

    void delete(CommentVote vote);
}

package com.example.paceleague.board.adapter.out.persistence;

import com.example.paceleague.board.application.port.out.CommentVoteRepositoryPort;
import com.example.paceleague.board.domain.entity.CommentVote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentVotePersistenceAdapter implements CommentVoteRepositoryPort {

    private final CommentVoteJpaRepository commentVoteJpaRepository;

    public CommentVote save(CommentVote vote) {
        return commentVoteJpaRepository.save(vote);
    }

    public Optional<CommentVote> findByCommentSnoAndMemberSno(Long commentSno, Long memberSno) {
        return commentVoteJpaRepository.findByCommentSnoAndMemberSno(commentSno, memberSno);
    }

    public void deleteByCommentSnoIn(List<Long> commentSnos) {
        commentVoteJpaRepository.deleteByCommentSnoIn(commentSnos);
    }

    public void delete(CommentVote vote) {
        commentVoteJpaRepository.delete(vote);
    }
}

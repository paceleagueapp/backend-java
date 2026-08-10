package com.example.paceleague.board.application.port.out;

import com.example.paceleague.board.domain.entity.PostVote;

import java.util.Optional;

public interface PostVoteRepositoryPort {
    PostVote save(PostVote vote);

    Optional<PostVote> findByPostSnoAndMemberSno(Long postSno, Long memberSno);

    void deleteByPostSno(Long postSno);

    void delete(PostVote vote);
}

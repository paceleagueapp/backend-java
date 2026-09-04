package com.paceleague.board.adapter.out.persistence;

import com.paceleague.board.application.port.out.PostVoteRepositoryPort;
import com.paceleague.board.domain.entity.PostVote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostVotePersistenceAdapter implements PostVoteRepositoryPort {

    private final PostVoteJpaRepository postVoteJpaRepository;

    public PostVote save(PostVote vote) {
        return postVoteJpaRepository.save(vote);
    }

    public Optional<PostVote> findByPostSnoAndMemberSno(Long postSno, Long memberSno) {
        return postVoteJpaRepository.findByPostSnoAndMemberSno(postSno, memberSno);
    }

    public void deleteByPostSno(Long postSno) {
        postVoteJpaRepository.deleteByPostSno(postSno);
    }

    public void delete(PostVote vote) {
        postVoteJpaRepository.delete(vote);
    }
}

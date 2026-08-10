package com.example.paceleague.board.adapter.out.persistence;

import com.example.paceleague.board.domain.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostVoteJpaRepository extends JpaRepository<PostVote, Long> {
    Optional<PostVote> findByPostSnoAndMemberSno(Long postSno, Long memberSno);

    void deleteByPostSno(Long postSno);
}

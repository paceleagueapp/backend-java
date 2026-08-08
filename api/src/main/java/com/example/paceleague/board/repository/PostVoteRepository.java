package com.example.paceleague.board.repository;

import com.example.paceleague.board.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    Optional<PostVote> findByPostSnoAndMemberSno(Long postSno, Long memberSno);

    void deleteByPostSno(Long postSno);
}

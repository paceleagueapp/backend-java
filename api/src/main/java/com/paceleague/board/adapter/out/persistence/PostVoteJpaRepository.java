package com.paceleague.board.adapter.out.persistence;

import com.paceleague.board.domain.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostVoteJpaRepository extends JpaRepository<PostVote, Long> {
    Optional<PostVote> findByPostSnoAndMemberSno(Long postSno, Long memberSno);

    void deleteByPostSno(Long postSno);

    @Modifying
    @Query("delete from PostVote v where v.memberSno = :memberSno")
    int deleteAllByMemberSno(@Param("memberSno") Long memberSno);
}

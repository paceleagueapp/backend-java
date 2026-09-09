package com.paceleague.board.adapter.out.persistence;

import com.paceleague.board.domain.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentVoteJpaRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByCommentSnoAndMemberSno(Long commentSno, Long memberSno);

    void deleteByCommentSnoIn(List<Long> commentSnos);

    @Modifying
    @Query("delete from CommentVote v where v.memberSno = :memberSno")
    int deleteAllByMemberSno(@Param("memberSno") Long memberSno);
}

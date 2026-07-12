package com.example.paceleague.ranking.repository;

import com.example.paceleague.rank.entity.MemberScore;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RankingRepository extends JpaRepository<MemberScore, Long> {
    Optional<MemberScore> findByMemberSnoAndSeasonSno(Long memberSno, Long seasonSno);

    long countBySeasonSnoAndTotalScoreGreaterThan(Long seasonSno, int totalScore);

    @Query("""
        select count(ms)
        from MemberScore ms
        where ms.seasonSno = :seasonSno
          and (
                ms.totalScore > :totalScore
             or (ms.totalScore = :totalScore and ms.updateAt < :updateAt)
             or (ms.totalScore = :totalScore and ms.updateAt = :updateAt and ms.memberSno < :memberSno)
          )
    """)
    long countHigherRankers(
            @Param("seasonSno") Long seasonSno,
            @Param("totalScore") int totalScore,
            @Param("updateAt") LocalDateTime updateAt,
            @Param("memberSno") Long memberSno
    );

    @Query(value = """
        select
            ms.member_sno as memberSno,
            m.nickname as nickname,
            ms.total_score as totalScore,
            ms.tier as tier
        from member_score ms
        join member m on ms.member_sno = m.sno
        where ms.season_sno = :seasonSno
        order by ms.total_score desc, ms.update_at asc, ms.member_sno asc
        limit 3
    """, nativeQuery = true)
    List<RankingProjection> findTop3(@Param("seasonSno") Long seasonSno);

    @Query(value = """
        select
            ms.member_sno as memberSno,
            m.nickname as nickname,
            ms.total_score as totalScore,
            ms.tier as tier
        from member_score ms
        join member m on ms.member_sno = m.sno
        where ms.season_sno = :seasonSno
        order by ms.total_score desc, ms.update_at asc, ms.member_sno asc
        limit :limit offset :offset
    """, nativeQuery = true)
    List<RankingProjection> findAroundRanking(
            @Param("seasonSno") Long seasonSno,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}

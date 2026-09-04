package com.paceleague.ranking.adapter.out.persistence;

// rank.adapter.out.persistence.MemberScoreJpaRepository와 별개로, ranking 도메인이 자신의 조회 방식(네이티브 쿼리 기반
// 순위 집계)을 위해 MemberScore를 대상으로 하는 두 번째 리포지토리를 의도적으로 유지한다 — 기존부터 있던 중복이며
// 이번 리팩토링에서 합치지 않고 그대로 보존함.
import com.paceleague.rank.domain.entity.MemberScore;
import com.paceleague.ranking.application.port.out.RankingProjection;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RankingJpaRepository extends JpaRepository<MemberScore, Long> {
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

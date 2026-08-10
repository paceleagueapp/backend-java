package com.example.paceleague.ranking.application.port.out;

import com.example.paceleague.rank.domain.entity.MemberScore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RankingRepositoryPort {
    Optional<MemberScore> findByMemberSnoAndSeasonSno(Long memberSno, Long seasonSno);

    long countBySeasonSnoAndTotalScoreGreaterThan(Long seasonSno, int totalScore);

    long countHigherRankers(Long seasonSno, int totalScore, LocalDateTime updateAt, Long memberSno);

    List<RankingProjection> findTop3(Long seasonSno);

    List<RankingProjection> findAroundRanking(Long seasonSno, int limit, int offset);
}

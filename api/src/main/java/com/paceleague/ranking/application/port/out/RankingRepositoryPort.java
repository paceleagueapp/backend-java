package com.paceleague.ranking.application.port.out;

import com.paceleague.rank.domain.entity.MemberScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RankingRepositoryPort {
    Optional<MemberScore> findByMemberSnoAndSeasonSno(Long memberSno, Long seasonSno);

    long countBySeasonSnoAndTotalScoreGreaterThan(Long seasonSno, int totalScore);

    long countHigherRankers(Long seasonSno, int totalScore, LocalDateTime updateAt, Long memberSno);

    List<RankingProjection> findTop3(Long seasonSno);

    List<RankingProjection> findAroundRanking(Long seasonSno, int limit, int offset);

    // 관리자 랭킹관리 화면 — 해당 시즌 전체를 점수 내림차순으로 페이지네이션.
    Page<RankingProjection> findAllBySeasonSno(Long seasonSno, Pageable pageable);
}

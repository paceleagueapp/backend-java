package com.paceleague.ranking.adapter.out.persistence;

import com.paceleague.rank.domain.entity.MemberScore;
import com.paceleague.ranking.application.port.out.RankingProjection;
import com.paceleague.ranking.application.port.out.RankingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RankingPersistenceAdapter implements RankingRepositoryPort {

    private final RankingJpaRepository rankingJpaRepository;

    public Optional<MemberScore> findByMemberSnoAndSeasonSno(Long memberSno, Long seasonSno) {
        return rankingJpaRepository.findByMemberSnoAndSeasonSno(memberSno, seasonSno);
    }

    public long countBySeasonSnoAndTotalScoreGreaterThan(Long seasonSno, int totalScore) {
        return rankingJpaRepository.countBySeasonSnoAndTotalScoreGreaterThan(seasonSno, totalScore);
    }

    public long countHigherRankers(Long seasonSno, int totalScore, LocalDateTime updateAt, Long memberSno) {
        return rankingJpaRepository.countHigherRankers(seasonSno, totalScore, updateAt, memberSno);
    }

    public List<RankingProjection> findTop3(Long seasonSno) {
        return rankingJpaRepository.findTop3(seasonSno);
    }

    public List<RankingProjection> findAroundRanking(Long seasonSno, int limit, int offset) {
        return rankingJpaRepository.findAroundRanking(seasonSno, limit, offset);
    }
}

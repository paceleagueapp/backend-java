package com.example.paceleague.rank.application.port.out;

import com.example.paceleague.rank.domain.entity.MemberScore;

import java.util.Optional;

public interface MemberScoreRepositoryPort {
    // @Lock(PESSIMISTIC_WRITE) — 어댑터 구현에서 그대로 보존
    Optional<MemberScore> findByMemberSnoAndSeasonSnoForUpdate(Long memberSno, Long seasonSno);

    Optional<MemberScore> findByMemberSnoAndSeasonSno(Long memberSno, Long seasonSno);

    MemberScore save(MemberScore memberScore);
}

package com.paceleague.rank.application.port.out;

import com.paceleague.rank.domain.entity.MemberScore;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberScoreRepositoryPort {
    // @Lock(PESSIMISTIC_WRITE) — 어댑터 구현에서 그대로 보존
    Optional<MemberScore> findByMemberSnoAndSeasonSnoForUpdate(Long memberSno, Long seasonSno);

    Optional<MemberScore> findByMemberSnoAndSeasonSno(Long memberSno, Long seasonSno);

    List<MemberScore> findByMemberSnosAndSeasonSno(Collection<Long> memberSnos, Long seasonSno);

    MemberScore save(MemberScore memberScore);
}

package com.example.paceleague.crew.application.port.out;

import com.example.paceleague.crew.application.port.in.GetMemberCrewBadgePort.CrewBadge;
import com.example.paceleague.crew.domain.entity.CrewMember;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CrewMemberRepositoryPort {

    CrewMember save(CrewMember crewMember);

    Optional<CrewMember> findByMemberSno(Long memberSno);

    Optional<CrewMember> findByCrewSnoAndMemberSno(Long crewSno, Long memberSno);

    List<CrewMember> findByCrewSnoOrderByJoinedAtAsc(Long crewSno);

    boolean existsByMemberSno(Long memberSno);

    // 여러 회원의 (크루명, 크루 아이콘) 배치 조회 — 크루 없는 회원은 결과에서 빠짐.
    Map<Long, CrewBadge> findBadgesByMemberSnos(Collection<Long> memberSnos);

    void delete(CrewMember crewMember);

    void deleteByCrewSno(Long crewSno);
}

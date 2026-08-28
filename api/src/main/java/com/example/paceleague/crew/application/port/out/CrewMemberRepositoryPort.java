package com.example.paceleague.crew.application.port.out;

import com.example.paceleague.crew.domain.entity.CrewMember;

import java.util.List;
import java.util.Optional;

public interface CrewMemberRepositoryPort {

    CrewMember save(CrewMember crewMember);

    Optional<CrewMember> findByMemberSno(Long memberSno);

    Optional<CrewMember> findByCrewSnoAndMemberSno(Long crewSno, Long memberSno);

    List<CrewMember> findByCrewSnoOrderByJoinedAtAsc(Long crewSno);

    boolean existsByMemberSno(Long memberSno);

    void delete(CrewMember crewMember);

    void deleteByCrewSno(Long crewSno);
}

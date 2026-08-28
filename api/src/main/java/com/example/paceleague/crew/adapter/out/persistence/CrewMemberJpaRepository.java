package com.example.paceleague.crew.adapter.out.persistence;

import com.example.paceleague.crew.domain.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrewMemberJpaRepository extends JpaRepository<CrewMember, Long> {

    Optional<CrewMember> findByMemberSno(Long memberSno);

    Optional<CrewMember> findByCrewSnoAndMemberSno(Long crewSno, Long memberSno);

    List<CrewMember> findByCrewSnoOrderByJoinedAtAsc(Long crewSno);

    boolean existsByMemberSno(Long memberSno);

    void deleteByCrewSno(Long crewSno);
}

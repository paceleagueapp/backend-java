package com.example.paceleague.crew.adapter.out.persistence;

import com.example.paceleague.crew.domain.entity.CrewJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrewJoinRequestJpaRepository extends JpaRepository<CrewJoinRequest, Long> {

    List<CrewJoinRequest> findByCrewSnoAndStatusOrderByCreateAtAsc(Long crewSno, String status);

    boolean existsByCrewSnoAndMemberSnoAndStatus(Long crewSno, Long memberSno, String status);

    void deleteByCrewSno(Long crewSno);
}

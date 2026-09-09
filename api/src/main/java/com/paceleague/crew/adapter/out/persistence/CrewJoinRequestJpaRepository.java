package com.paceleague.crew.adapter.out.persistence;

import com.paceleague.crew.domain.entity.CrewJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewJoinRequestJpaRepository extends JpaRepository<CrewJoinRequest, Long> {

    List<CrewJoinRequest> findByCrewSnoAndStatusOrderByCreateAtAsc(Long crewSno, String status);

    boolean existsByCrewSnoAndMemberSnoAndStatus(Long crewSno, Long memberSno, String status);

    void deleteByCrewSno(Long crewSno);

    @Modifying
    @Query("delete from CrewJoinRequest r where r.memberSno = :m")
    int deleteAllByMemberSno(@Param("m") Long memberSno);
}

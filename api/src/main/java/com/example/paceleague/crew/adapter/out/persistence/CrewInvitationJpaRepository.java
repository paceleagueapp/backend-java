package com.example.paceleague.crew.adapter.out.persistence;

import com.example.paceleague.crew.domain.entity.CrewInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrewInvitationJpaRepository extends JpaRepository<CrewInvitation, Long> {

    List<CrewInvitation> findByInviteeMemberSnoAndStatusOrderByCreateAtDesc(Long inviteeMemberSno, String status);

    boolean existsByCrewSnoAndInviteeMemberSnoAndStatus(Long crewSno, Long inviteeMemberSno, String status);

    void deleteByCrewSno(Long crewSno);
}

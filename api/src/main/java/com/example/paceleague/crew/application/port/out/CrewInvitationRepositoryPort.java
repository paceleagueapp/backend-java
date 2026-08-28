package com.example.paceleague.crew.application.port.out;

import com.example.paceleague.crew.domain.entity.CrewInvitation;

import java.util.List;
import java.util.Optional;

public interface CrewInvitationRepositoryPort {

    CrewInvitation save(CrewInvitation invitation);

    Optional<CrewInvitation> findBySno(Long sno);

    // 내가 받은 PENDING 초대(최신순).
    List<CrewInvitation> findPendingByInvitee(Long inviteeMemberSno);

    boolean existsPendingByCrewSnoAndInvitee(Long crewSno, Long inviteeMemberSno);

    void deleteByCrewSno(Long crewSno);
}

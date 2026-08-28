package com.example.paceleague.crew.adapter.out.persistence;

import com.example.paceleague.crew.application.port.out.CrewInvitationRepositoryPort;
import com.example.paceleague.crew.domain.entity.CrewInvitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CrewInvitationPersistenceAdapter implements CrewInvitationRepositoryPort {

    private final CrewInvitationJpaRepository crewInvitationJpaRepository;

    public CrewInvitation save(CrewInvitation invitation) {
        return crewInvitationJpaRepository.save(invitation);
    }

    public Optional<CrewInvitation> findBySno(Long sno) {
        return crewInvitationJpaRepository.findById(sno);
    }

    public List<CrewInvitation> findPendingByInvitee(Long inviteeMemberSno) {
        return crewInvitationJpaRepository.findByInviteeMemberSnoAndStatusOrderByCreateAtDesc(
                inviteeMemberSno, CrewInvitation.STATUS_PENDING);
    }

    public boolean existsPendingByCrewSnoAndInvitee(Long crewSno, Long inviteeMemberSno) {
        return crewInvitationJpaRepository.existsByCrewSnoAndInviteeMemberSnoAndStatus(
                crewSno, inviteeMemberSno, CrewInvitation.STATUS_PENDING);
    }

    public void deleteByCrewSno(Long crewSno) {
        crewInvitationJpaRepository.deleteByCrewSno(crewSno);
    }
}

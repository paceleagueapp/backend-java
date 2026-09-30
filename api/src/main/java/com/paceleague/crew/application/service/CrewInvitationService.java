package com.paceleague.crew.application.service;

import com.paceleague.crew.application.dto.CrewInvitationResponse;
import com.paceleague.crew.application.port.in.CrewInvitationUseCase;
import com.paceleague.crew.application.port.out.CrewInvitationRepositoryPort;
import com.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.paceleague.crew.config.CrewProperties;
import com.paceleague.crew.domain.entity.Crew;
import com.paceleague.crew.domain.entity.CrewInvitation;
import com.paceleague.crew.domain.policy.CrewMembershipPolicy;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewInvitationService implements CrewInvitationUseCase {

    private final CrewRepositoryPort crewRepositoryPort;
    private final CrewMemberRepositoryPort crewMemberRepositoryPort;
    private final CrewInvitationRepositoryPort crewInvitationRepositoryPort;
    private final CrewMembershipManager membershipManager;
    private final GetMemberNicknamePort getMemberNicknamePort;
    private final CrewProperties props;

    @Override
    public Long invite(Long leaderMemberSno, Long crewSno, Long inviteeMemberSno) {
        Crew crew = getCrew(crewSno);
        CrewMembershipPolicy.assertLeader(crew, leaderMemberSno);

        if (leaderMemberSno.equals(inviteeMemberSno)) {
            throw new IllegalArgumentException("You cannot invite yourself.");
        }
        if (crewMemberRepositoryPort.existsByMemberSno(inviteeMemberSno)) {
            throw new IllegalArgumentException("This member already belongs to another crew.");
        }
        if (crewInvitationRepositoryPort.existsPendingByCrewSnoAndInvitee(crewSno, inviteeMemberSno)) {
            throw new IllegalArgumentException("This member has already been invited.");
        }
        if (crew.isFull()) {
            throw new IllegalArgumentException("This crew is full.");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(props.invitationExpireDays());
        return crewInvitationRepositoryPort
                .save(CrewInvitation.create(crewSno, leaderMemberSno, inviteeMemberSno, expiresAt))
                .getSno();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrewInvitationResponse> listMyInvitations(Long memberSno) {
        LocalDateTime now = LocalDateTime.now();
        return crewInvitationRepositoryPort.findPendingByInvitee(memberSno).stream()
                .filter(inv -> !inv.isExpired(now))
                .map(inv -> {
                    Crew crew = crewRepositoryPort.findBySno(inv.getCrewSno()).orElse(null);
                    return new CrewInvitationResponse(
                            inv.getSno(),
                            inv.getCrewSno(),
                            crew == null ? "(삭제된 크루)" : crew.getName(),
                            crew == null ? null : crew.getIconUrl(),
                            getMemberNicknamePort.getNickname(inv.getInviterMemberSno()),
                            inv.getStatus(),
                            inv.getCreateAt(),
                            inv.getExpiresAt());
                })
                .toList();
    }

    @Override
    public void accept(Long memberSno, Long invitationId) {
        CrewInvitation inv = getInvitation(invitationId);
        if (!inv.getInviteeMemberSno().equals(memberSno)) {
            throw new IllegalArgumentException("This invitation was not sent to you.");
        }
        if (!inv.isPending()) {
            throw new IllegalArgumentException("This invitation has already been processed.");
        }
        if (inv.isExpired(LocalDateTime.now())) {
            inv.expire();
            crewInvitationRepositoryPort.save(inv);
            throw new IllegalArgumentException("This invitation has expired.");
        }

        membershipManager.joinCrew(inv.getCrewSno(), memberSno);
        inv.accept();
        crewInvitationRepositoryPort.save(inv);
    }

    @Override
    public void decline(Long memberSno, Long invitationId) {
        CrewInvitation inv = getInvitation(invitationId);
        if (!inv.getInviteeMemberSno().equals(memberSno)) {
            throw new IllegalArgumentException("This invitation was not sent to you.");
        }
        if (!inv.isPending()) {
            throw new IllegalArgumentException("This invitation has already been processed.");
        }
        inv.decline();
        crewInvitationRepositoryPort.save(inv);
    }

    @Override
    public void cancel(Long leaderMemberSno, Long invitationId) {
        CrewInvitation inv = getInvitation(invitationId);
        Crew crew = getCrew(inv.getCrewSno());
        CrewMembershipPolicy.assertLeader(crew, leaderMemberSno);
        if (!inv.isPending()) {
            throw new IllegalArgumentException("This invitation has already been processed.");
        }
        inv.cancel();
        crewInvitationRepositoryPort.save(inv);
    }

    private Crew getCrew(Long crewSno) {
        return crewRepositoryPort.findBySno(crewSno)
                .orElseThrow(() -> new IllegalArgumentException("Crew not found."));
    }

    private CrewInvitation getInvitation(Long id) {
        return crewInvitationRepositoryPort.findBySno(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found."));
    }
}

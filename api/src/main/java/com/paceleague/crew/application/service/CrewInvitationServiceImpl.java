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
import com.paceleague.member.application.port.in.GetMemberNicknamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewInvitationServiceImpl implements CrewInvitationUseCase {

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
            throw new IllegalArgumentException("자기 자신은 초대할 수 없습니다");
        }
        if (crewMemberRepositoryPort.existsByMemberSno(inviteeMemberSno)) {
            throw new IllegalArgumentException("이미 다른 크루에 소속된 회원입니다");
        }
        if (crewInvitationRepositoryPort.existsPendingByCrewSnoAndInvitee(crewSno, inviteeMemberSno)) {
            throw new IllegalArgumentException("이미 초대한 회원입니다");
        }
        if (crew.isFull()) {
            throw new IllegalArgumentException("크루 정원이 가득 찼습니다");
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
            throw new IllegalArgumentException("본인이 받은 초대가 아닙니다");
        }
        if (!inv.isPending()) {
            throw new IllegalArgumentException("이미 처리된 초대입니다");
        }
        if (inv.isExpired(LocalDateTime.now())) {
            inv.expire();
            crewInvitationRepositoryPort.save(inv);
            throw new IllegalArgumentException("만료된 초대입니다");
        }

        membershipManager.joinCrew(inv.getCrewSno(), memberSno);
        inv.accept();
        crewInvitationRepositoryPort.save(inv);
    }

    @Override
    public void decline(Long memberSno, Long invitationId) {
        CrewInvitation inv = getInvitation(invitationId);
        if (!inv.getInviteeMemberSno().equals(memberSno)) {
            throw new IllegalArgumentException("본인이 받은 초대가 아닙니다");
        }
        if (!inv.isPending()) {
            throw new IllegalArgumentException("이미 처리된 초대입니다");
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
            throw new IllegalArgumentException("이미 처리된 초대입니다");
        }
        inv.cancel();
        crewInvitationRepositoryPort.save(inv);
    }

    private Crew getCrew(Long crewSno) {
        return crewRepositoryPort.findBySno(crewSno)
                .orElseThrow(() -> new IllegalArgumentException("크루를 찾을 수 없습니다"));
    }

    private CrewInvitation getInvitation(Long id) {
        return crewInvitationRepositoryPort.findBySno(id)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다"));
    }
}

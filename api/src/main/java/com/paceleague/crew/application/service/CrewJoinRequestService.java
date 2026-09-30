package com.paceleague.crew.application.service;

import com.paceleague.crew.application.dto.CrewJoinRequestResponse;
import com.paceleague.crew.application.port.in.CrewJoinRequestUseCase;
import com.paceleague.crew.application.port.out.CrewJoinRequestRepositoryPort;
import com.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.paceleague.crew.domain.entity.Crew;
import com.paceleague.crew.domain.entity.CrewJoinRequest;
import com.paceleague.crew.domain.policy.CrewMembershipPolicy;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewJoinRequestService implements CrewJoinRequestUseCase {

    private static final int MESSAGE_MAX_LENGTH = 300;

    private final CrewRepositoryPort crewRepositoryPort;
    private final CrewMemberRepositoryPort crewMemberRepositoryPort;
    private final CrewJoinRequestRepositoryPort crewJoinRequestRepositoryPort;
    private final CrewMembershipManager membershipManager;
    private final GetMemberNicknamePort getMemberNicknamePort;

    @Override
    public Long apply(Long memberSno, Long crewSno, String message) {
        if (crewMemberRepositoryPort.existsByMemberSno(memberSno)) {
            throw new IllegalArgumentException("You are already in a crew.");
        }
        Crew crew = getCrew(crewSno);
        if (crew.isFull()) {
            throw new IllegalArgumentException("This crew is full.");
        }
        if (crewJoinRequestRepositoryPort.existsPendingByCrewSnoAndMemberSno(crewSno, memberSno)) {
            throw new IllegalArgumentException("You already have a pending join request.");
        }

        String msg = (message == null || message.isBlank()) ? null : message.trim();
        if (msg != null && msg.length() > MESSAGE_MAX_LENGTH) {
            throw new IllegalArgumentException("Message is too long (max " + MESSAGE_MAX_LENGTH + " characters).");
        }
        return crewJoinRequestRepositoryPort.save(CrewJoinRequest.create(crewSno, memberSno, msg)).getSno();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrewJoinRequestResponse> listPending(Long leaderMemberSno, Long crewSno) {
        Crew crew = getCrew(crewSno);
        CrewMembershipPolicy.assertLeader(crew, leaderMemberSno);
        return crewJoinRequestRepositoryPort.findPendingByCrewSno(crewSno).stream()
                .map(r -> new CrewJoinRequestResponse(
                        r.getSno(),
                        r.getMemberSno(),
                        getMemberNicknamePort.getNickname(r.getMemberSno()),
                        r.getMessage(),
                        r.getStatus(),
                        r.getCreateAt()))
                .toList();
    }

    @Override
    public void approve(Long leaderMemberSno, Long joinRequestId) {
        CrewJoinRequest jr = getJoinRequest(joinRequestId);
        Crew crew = getCrew(jr.getCrewSno());
        CrewMembershipPolicy.assertLeader(crew, leaderMemberSno);
        if (!jr.isPending()) {
            throw new IllegalArgumentException("This join request has already been processed.");
        }
        membershipManager.joinCrew(jr.getCrewSno(), jr.getMemberSno());
        jr.approve();
        crewJoinRequestRepositoryPort.save(jr);
    }

    @Override
    public void reject(Long leaderMemberSno, Long joinRequestId) {
        CrewJoinRequest jr = getJoinRequest(joinRequestId);
        Crew crew = getCrew(jr.getCrewSno());
        CrewMembershipPolicy.assertLeader(crew, leaderMemberSno);
        if (!jr.isPending()) {
            throw new IllegalArgumentException("This join request has already been processed.");
        }
        jr.reject();
        crewJoinRequestRepositoryPort.save(jr);
    }

    @Override
    public void cancel(Long memberSno, Long joinRequestId) {
        CrewJoinRequest jr = getJoinRequest(joinRequestId);
        if (!jr.getMemberSno().equals(memberSno)) {
            throw new IllegalArgumentException("This is not your join request.");
        }
        if (!jr.isPending()) {
            throw new IllegalArgumentException("This join request has already been processed.");
        }
        jr.cancel();
        crewJoinRequestRepositoryPort.save(jr);
    }

    private Crew getCrew(Long crewSno) {
        return crewRepositoryPort.findBySno(crewSno)
                .orElseThrow(() -> new IllegalArgumentException("Crew not found."));
    }

    private CrewJoinRequest getJoinRequest(Long id) {
        return crewJoinRequestRepositoryPort.findBySno(id)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found."));
    }
}

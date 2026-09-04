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
            throw new IllegalArgumentException("이미 크루에 소속되어 있습니다");
        }
        Crew crew = getCrew(crewSno);
        if (crew.isFull()) {
            throw new IllegalArgumentException("크루 정원이 가득 찼습니다");
        }
        if (crewJoinRequestRepositoryPort.existsPendingByCrewSnoAndMemberSno(crewSno, memberSno)) {
            throw new IllegalArgumentException("이미 가입신청 중입니다");
        }

        String msg = (message == null || message.isBlank()) ? null : message.trim();
        if (msg != null && msg.length() > MESSAGE_MAX_LENGTH) {
            throw new IllegalArgumentException("메시지가 너무 깁니다 (최대 " + MESSAGE_MAX_LENGTH + "자)");
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
            throw new IllegalArgumentException("이미 처리된 신청입니다");
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
            throw new IllegalArgumentException("이미 처리된 신청입니다");
        }
        jr.reject();
        crewJoinRequestRepositoryPort.save(jr);
    }

    @Override
    public void cancel(Long memberSno, Long joinRequestId) {
        CrewJoinRequest jr = getJoinRequest(joinRequestId);
        if (!jr.getMemberSno().equals(memberSno)) {
            throw new IllegalArgumentException("본인 신청이 아닙니다");
        }
        if (!jr.isPending()) {
            throw new IllegalArgumentException("이미 처리된 신청입니다");
        }
        jr.cancel();
        crewJoinRequestRepositoryPort.save(jr);
    }

    private Crew getCrew(Long crewSno) {
        return crewRepositoryPort.findBySno(crewSno)
                .orElseThrow(() -> new IllegalArgumentException("크루를 찾을 수 없습니다"));
    }

    private CrewJoinRequest getJoinRequest(Long id) {
        return crewJoinRequestRepositoryPort.findBySno(id)
                .orElseThrow(() -> new IllegalArgumentException("가입신청을 찾을 수 없습니다"));
    }
}

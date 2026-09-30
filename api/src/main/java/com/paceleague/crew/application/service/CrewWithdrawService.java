package com.paceleague.crew.application.service;

import com.paceleague.crew.application.port.in.shared.LeaveCrewOnWithdrawPort;
import com.paceleague.crew.application.port.out.CrewInvitationRepositoryPort;
import com.paceleague.crew.application.port.out.CrewJoinRequestRepositoryPort;
import com.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.paceleague.crew.domain.entity.Crew;
import com.paceleague.crew.domain.entity.CrewMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrewWithdrawService implements LeaveCrewOnWithdrawPort {

    private final CrewMemberRepositoryPort crewMemberRepositoryPort;
    private final CrewRepositoryPort crewRepositoryPort;
    private final CrewInvitationRepositoryPort crewInvitationRepositoryPort;
    private final CrewJoinRequestRepositoryPort crewJoinRequestRepositoryPort;

    @Override
    @Transactional
    public void onMemberWithdraw(Long memberSno) {
        crewMemberRepositoryPort.findByMemberSno(memberSno).ifPresent(cm -> {
            Crew crew = crewRepositoryPort.findBySnoForUpdate(cm.getCrewSno())
                    .orElseThrow(() -> new IllegalArgumentException("Crew not found."));
            if (crew.isLeader(memberSno)) {
                throw new IllegalArgumentException("As crew leader, you must transfer leadership or disband the crew before deleting your account.");
            }
            crewMemberRepositoryPort.delete(cm);
            crew.decreaseMemberCount();
            crewRepositoryPort.save(crew);
        });
        crewInvitationRepositoryPort.deleteAllByMember(memberSno);
        crewJoinRequestRepositoryPort.deleteAllByMemberSno(memberSno);
    }
}

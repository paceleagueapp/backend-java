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
                    .orElseThrow(() -> new IllegalArgumentException("크루를 찾을 수 없습니다"));
            if (crew.isLeader(memberSno)) {
                throw new IllegalArgumentException("크루장은 크루를 먼저 위임하거나 해체한 뒤 탈퇴할 수 있습니다.");
            }
            crewMemberRepositoryPort.delete(cm);
            crew.decreaseMemberCount();
            crewRepositoryPort.save(crew);
        });
        crewInvitationRepositoryPort.deleteAllByMember(memberSno);
        crewJoinRequestRepositoryPort.deleteAllByMemberSno(memberSno);
    }
}

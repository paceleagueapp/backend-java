package com.paceleague.crew.application.port.out;

import com.paceleague.crew.domain.entity.CrewInvitation;

import java.util.List;
import java.util.Optional;

public interface CrewInvitationRepositoryPort {

    CrewInvitation save(CrewInvitation invitation);

    Optional<CrewInvitation> findBySno(Long sno);

    // 내가 받은 PENDING 초대(최신순).
    List<CrewInvitation> findPendingByInvitee(Long inviteeMemberSno);

    boolean existsPendingByCrewSnoAndInvitee(Long crewSno, Long inviteeMemberSno);

    void deleteByCrewSno(Long crewSno);

    // 회원 탈퇴 시 그 회원이 보내거나 받은 초대 전부 삭제.
    void deleteAllByMember(Long memberSno);
}

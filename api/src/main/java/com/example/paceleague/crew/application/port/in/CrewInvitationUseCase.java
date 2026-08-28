package com.example.paceleague.crew.application.port.in;

import com.example.paceleague.crew.application.dto.CrewInvitationResponse;

import java.util.List;

public interface CrewInvitationUseCase {

    // 크루장: 회원 초대
    Long invite(Long leaderMemberSno, Long crewSno, Long inviteeMemberSno);

    // 초대받은 회원: 내가 받은 PENDING 초대 목록
    List<CrewInvitationResponse> listMyInvitations(Long memberSno);

    // 초대받은 회원: 수락 → 크루 가입
    void accept(Long memberSno, Long invitationId);

    // 초대받은 회원: 거절
    void decline(Long memberSno, Long invitationId);

    // 크루장: 보낸 초대 취소
    void cancel(Long leaderMemberSno, Long invitationId);
}

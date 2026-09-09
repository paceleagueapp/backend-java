package com.paceleague.member.application.port.in;

import com.paceleague.member.application.dto.BlockedMemberResponse;

import java.util.List;

public interface MemberBlockUseCase {
    void block(Long blockerMemberSno, Long blockedMemberSno);

    void unblock(Long blockerMemberSno, Long blockedMemberSno);

    List<BlockedMemberResponse> list(Long blockerMemberSno);
}

package com.paceleague.member.application.port.out;

import com.paceleague.member.domain.entity.MemberBlock;

import java.util.List;
import java.util.Set;

public interface MemberBlockRepositoryPort {
    MemberBlock save(MemberBlock block);

    boolean exists(Long blockerMemberSno, Long blockedMemberSno);

    void delete(Long blockerMemberSno, Long blockedMemberSno);

    List<MemberBlock> findByBlocker(Long blockerMemberSno);

    Set<Long> findBlockedSnos(Long blockerMemberSno);

    // 회원 탈퇴 시 — 내가 차단했거나 나를 차단한 행 전부.
    void deleteAllByMember(Long memberSno);
}

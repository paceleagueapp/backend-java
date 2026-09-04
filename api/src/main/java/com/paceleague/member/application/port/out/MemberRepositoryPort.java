package com.paceleague.member.application.port.out;

import com.paceleague.member.domain.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepositoryPort {
    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findBySno(Long sno);

    boolean existsByMemberId(String memberId);

    Member save(Member member);

    // member_id 접두 일치 또는 nickname 부분 일치. 아이디 우선, 최대 limit개.
    List<Member> searchByMemberIdOrNickname(String query, int limit);
}

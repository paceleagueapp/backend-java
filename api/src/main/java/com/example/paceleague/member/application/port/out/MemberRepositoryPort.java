package com.example.paceleague.member.application.port.out;

import com.example.paceleague.member.domain.entity.Member;

import java.util.Optional;

public interface MemberRepositoryPort {
    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findBySno(Long sno);

    boolean existsByMemberId(String memberId);

    Member save(Member member);
}

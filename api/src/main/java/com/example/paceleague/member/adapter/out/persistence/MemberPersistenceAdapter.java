package com.example.paceleague.member.adapter.out.persistence;

import com.example.paceleague.member.application.port.out.MemberRepositoryPort;
import com.example.paceleague.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberRepositoryPort {

    private final MemberJpaRepository memberJpaRepository;

    public Optional<Member> findByMemberId(String memberId) {
        return memberJpaRepository.findByMemberId(memberId);
    }

    public Optional<Member> findBySno(Long sno) {
        return memberJpaRepository.findBySno(sno);
    }

    public boolean existsByMemberId(String memberId) {
        return memberJpaRepository.existsByMemberId(memberId);
    }

    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }
}

package com.example.paceleague.member.adapter.out.persistence;

import com.example.paceleague.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findBySno(Long sno);

    boolean existsByMemberId(String memberId);
}

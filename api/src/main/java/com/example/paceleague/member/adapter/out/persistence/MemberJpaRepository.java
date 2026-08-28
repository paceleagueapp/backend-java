package com.example.paceleague.member.adapter.out.persistence;

import com.example.paceleague.member.domain.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findBySno(Long sno);

    boolean existsByMemberId(String memberId);

    @Query("""
            select m from Member m
            where lower(m.memberId) like lower(concat(:q, '%'))
               or lower(m.nickname) like lower(concat('%', :q, '%'))
            order by case when lower(m.memberId) = lower(:q) then 0
                          when lower(m.memberId) like lower(concat(:q, '%')) then 1
                          else 2 end,
                     m.memberId asc
            """)
    List<Member> searchByMemberIdOrNickname(@Param("q") String q, Pageable pageable);
}

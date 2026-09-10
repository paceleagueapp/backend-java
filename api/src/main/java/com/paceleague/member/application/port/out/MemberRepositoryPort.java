package com.paceleague.member.application.port.out;

import com.paceleague.member.domain.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepositoryPort {
    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findBySno(Long sno);

    boolean existsByMemberId(String memberId);

    Member save(Member member);

    // member_id 접두 일치 또는 nickname 부분 일치. 아이디 우선, 최대 limit개.
    List<Member> searchByMemberIdOrNickname(String query, int limit);

    // 관리자 회원관리 목록 — query가 빈 문자열이면 전체를 페이지네이션해서 반환(탈퇴 회원 포함, 운영자는 봐야 하므로).
    Page<Member> searchForAdmin(String query, Pageable pageable);
}

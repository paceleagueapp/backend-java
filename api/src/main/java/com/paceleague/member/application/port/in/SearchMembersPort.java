package com.paceleague.member.application.port.in;

import com.paceleague.member.application.dto.MemberSearchResult;

import java.util.List;

// 크루 초대 등에서 아이디/닉네임으로 회원을 찾기 위한 크로스 도메인 포트.
public interface SearchMembersPort {
    List<MemberSearchResult> search(String query, int limit);
}

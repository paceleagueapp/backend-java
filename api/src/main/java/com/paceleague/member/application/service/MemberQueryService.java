package com.paceleague.member.application.service;

import com.paceleague.member.application.dto.MemberSearchResult;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.member.application.port.in.SearchMembersPort;
import com.paceleague.member.application.port.out.MemberRepositoryPort;
import com.paceleague.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService implements GetMemberNicknamePort, SearchMembersPort {

    private final MemberRepositoryPort memberRepositoryPort;

    // 탈퇴 회원(WITHDRAWN)은 닉네임이 지워져 있으므로 "탈퇴한 사용자"로 표시한다.
    // 이 한 곳만으로 board(작성자/댓글) · territory(지도) · ranking 전부에 반영된다.
    static final String WITHDRAWN_DISPLAY_NAME = "탈퇴한 사용자";

    public String getNickname(Long memberSno) {
        return memberRepositoryPort.findBySno(memberSno)
                .map(m -> m.isActive() && m.getNickname() != null ? m.getNickname() : WITHDRAWN_DISPLAY_NAME)
                .orElse("알 수 없음");
    }

    public List<MemberSearchResult> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return memberRepositoryPort.searchByMemberIdOrNickname(query.trim(), limit)
                .stream()
                .filter(Member::isActive)
                .map(m -> new MemberSearchResult(m.getSno().longValue(), m.getMemberId(), m.getNickname()))
                .toList();
    }
}

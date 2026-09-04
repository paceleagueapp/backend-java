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

    public String getNickname(Long memberSno) {
        return memberRepositoryPort.findBySno(memberSno)
                .map(Member::getNickname)
                .orElse("알 수 없음");
    }

    public List<MemberSearchResult> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return memberRepositoryPort.searchByMemberIdOrNickname(query.trim(), limit)
                .stream()
                .map(m -> new MemberSearchResult(m.getSno().longValue(), m.getMemberId(), m.getNickname()))
                .toList();
    }
}

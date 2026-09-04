package com.paceleague.member.application.dto;

public record MemberSearchResult(
        Long memberSno,
        String memberId,
        String nickname
) {
}

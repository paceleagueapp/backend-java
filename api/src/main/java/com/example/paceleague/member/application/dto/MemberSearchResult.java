package com.example.paceleague.member.application.dto;

public record MemberSearchResult(
        Long memberSno,
        String memberId,
        String nickname
) {
}

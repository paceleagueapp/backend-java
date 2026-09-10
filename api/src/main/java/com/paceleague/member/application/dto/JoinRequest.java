package com.paceleague.member.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JoinRequest (
        @NotBlank @Size(max = 50) String memberId,
        @NotBlank @Size(min = 8, max = 100) String password,
        @Size(max = 50) String nickname,
        @Size(max = 50) String email,
        // 이용약관/개인정보처리방침/위치정보 수집·이용 — 셋 다 true여야 가입 가능(MemberAuthService에서 검증).
        @NotNull Boolean agreedTerms,
        @NotNull Boolean agreedPrivacy,
        @NotNull Boolean agreedLocation
){}

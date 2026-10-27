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
        @NotNull Boolean agreedLocation,
        // 선택 동의 — 서비스 알림/마케팅 정보 수신/야간(21~08시) 마케팅 수신. null 이면 기록하지 않고
        // 첫 로그인 때 알림 동의 화면에서 다시 묻는다(이 값을 보내지 않는 기존 앱 호환).
        Boolean agreedPushService,
        Boolean agreedMarketing,
        Boolean agreedMarketingNight
){}

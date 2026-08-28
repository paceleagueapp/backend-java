package com.example.paceleague.crew.domain.policy;

// 크루명 검증 — 순수 로직. 실패는 IllegalArgumentException(→ 400).
public final class CrewNamePolicy {

    private CrewNamePolicy() {
    }

    // 앞뒤 공백을 다듬은 크루명을 돌려준다. 길이/공백 위반이면 예외.
    public static String normalizeAndValidate(String rawName, int minLength, int maxLength) {
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("크루명을 입력해주세요");
        }
        String name = rawName.trim();
        if (name.length() < minLength || name.length() > maxLength) {
            throw new IllegalArgumentException("크루명은 " + minLength + "~" + maxLength + "자여야 합니다");
        }
        return name;
    }
}

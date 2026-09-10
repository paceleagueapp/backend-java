package com.paceleague.member.domain.entity;

// 동의 유형. currentVersion은 약관 문서를 개정할 때마다 여기 값을 올려 재동의 필요 여부를 판단할 수 있게 한다.
// TERMS/PRIVACY/LOCATION은 회원가입 시 필수, 나머지 세 알림 동의는 로그인 시점에 별도로 받는다.
public enum AgreementType {
    TERMS("2026-09-10", true),
    PRIVACY("2026-09-10", true),
    LOCATION("2026-09-10", true),
    PUSH_SERVICE("2026-09-10", false),
    MARKETING("2026-09-10", false),
    MARKETING_NIGHT("2026-09-10", false);

    private final String currentVersion;
    private final boolean requiredAtJoin;

    AgreementType(String currentVersion, boolean requiredAtJoin) {
        this.currentVersion = currentVersion;
        this.requiredAtJoin = requiredAtJoin;
    }

    public String currentVersion() {
        return currentVersion;
    }

    public boolean isRequiredAtJoin() {
        return requiredAtJoin;
    }
}

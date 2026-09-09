package com.paceleague.crew.application.port.in.shared;

// 회원 탈퇴 시 크루 관계 정리. 크루장이면 IllegalArgumentException(먼저 위임/해체) — 탈퇴 자체가 400 으로 막힌다.
public interface LeaveCrewOnWithdrawPort {
    void onMemberWithdraw(Long memberSno);
}

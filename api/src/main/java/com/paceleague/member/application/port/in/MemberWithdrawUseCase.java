package com.paceleague.member.application.port.in;

// 회원 탈퇴 — 비밀번호 재확인 후, 러닝/건강/랭킹/땅 데이터를 같은 트랜잭션에서 삭제하고
// member 행은 개인정보를 지운 채 WITHDRAWN 으로 남긴다(글/댓글은 익명 표시로 유지).
public interface MemberWithdrawUseCase {
    void withdraw(Long memberSno, String rawPassword);
}

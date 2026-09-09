package com.paceleague.rank.application.port.in.shared;

// 회원 탈퇴 시 그 회원의 점수 로그·시즌 누적 점수를 삭제한다(랭킹에서 제외).
public interface PurgeMemberRankPort {
    void purge(Long memberSno);
}

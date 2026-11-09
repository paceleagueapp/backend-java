package com.paceleague.member.application.port.in.shared;


import java.util.Collection;
import java.util.Map;

public interface GetMemberNicknamePort {
    String getNickname(Long memberSno);

    // 여러 명을 한 번에 — 표시 규칙은 getNickname 과 같다(탈퇴/없는 회원 포함, 모든 sno 가 키로 들어간다).
    Map<Long, String> getNicknames(Collection<Long> memberSnos);
}

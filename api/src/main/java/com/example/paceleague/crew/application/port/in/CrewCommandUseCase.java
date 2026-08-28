package com.example.paceleague.crew.application.port.in;

import com.example.paceleague.crew.application.dto.CrewCreateRequest;
import com.example.paceleague.crew.application.dto.CrewUpdateRequest;

// 크루 생성/수정/해체 + 크루원 관리(탈퇴/추방/위임).
public interface CrewCommandUseCase {

    Long create(Long memberSno, CrewCreateRequest req);

    // 크루장: 이름·아이콘·소개·공지 갱신
    void update(Long memberSno, Long crewSno, CrewUpdateRequest req);

    // 크루장: 크루 해체 (혼자 남았을 때만)
    void disband(Long memberSno, Long crewSno);

    // 크루원: 스스로 탈퇴 (크루장은 위임/해체 먼저)
    void leave(Long memberSno, Long crewSno);

    // 크루장: 크루원 추방
    void kick(Long leaderMemberSno, Long crewSno, Long targetMemberSno);

    // 크루장: 크루장 위임 (기존 크루장은 일반 크루원으로 강등)
    void transferLeader(Long leaderMemberSno, Long crewSno, Long targetMemberSno);
}

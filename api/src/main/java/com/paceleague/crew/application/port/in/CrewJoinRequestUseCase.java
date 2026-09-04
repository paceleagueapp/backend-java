package com.paceleague.crew.application.port.in;

import com.paceleague.crew.application.dto.CrewJoinRequestResponse;

import java.util.List;

public interface CrewJoinRequestUseCase {

    // 크루 없는 회원: 가입신청
    Long apply(Long memberSno, Long crewSno, String message);

    // 크루장: 우리 크루로 온 PENDING 신청 목록
    List<CrewJoinRequestResponse> listPending(Long leaderMemberSno, Long crewSno);

    // 크루장: 승인 → 신청자 크루 가입
    void approve(Long leaderMemberSno, Long joinRequestId);

    // 크루장: 거절
    void reject(Long leaderMemberSno, Long joinRequestId);

    // 신청자: 신청 취소
    void cancel(Long memberSno, Long joinRequestId);
}

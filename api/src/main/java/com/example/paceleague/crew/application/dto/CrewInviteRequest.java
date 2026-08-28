package com.example.paceleague.crew.application.dto;

// 크루장이 초대할 회원. GET /api/member/search 로 찾은 회원의 memberSno.
public record CrewInviteRequest(Long inviteeMemberSno) {
}

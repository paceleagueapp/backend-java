package com.paceleague.crew.application.dto;

import java.util.List;

// 크루 상세. 공개 정보 + (요청자가 크루원이면) 공지·크루원 목록.
//  - viewerIsLeader / viewerIsMember: 요청자 관점 플래그. 비로그인/비크루원이면 둘 다 false, members 는 빈 목록.
public record CrewDetailResponse(
        Long sno,
        String name,
        String iconUrl,
        String description,
        String notice,
        int memberCount,
        int memberLimit,
        String joinPolicy,
        Long leaderMemberSno,
        boolean viewerIsLeader,
        boolean viewerIsMember,
        List<CrewMemberResponse> members
) {
}

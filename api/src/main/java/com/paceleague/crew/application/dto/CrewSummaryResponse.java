package com.paceleague.crew.application.dto;

// 크루 검색 결과 / 목록 한 건. 공개 정보만.
public record CrewSummaryResponse(
        Long sno,
        String name,
        String iconUrl,
        String description,
        int memberCount,
        int memberLimit,
        String joinPolicy
) {
}

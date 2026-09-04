package com.paceleague.crew.application.dto;

import com.paceleague.rank.domain.enums.RankTier;

// 크루원 랭킹 한 행. 현재 시즌 누적 점수 기준 내림차순.
public record CrewRankingEntryResponse(
        int rank,
        Long memberSno,
        String nickname,
        int totalScore,
        RankTier tier,
        String tierLabel,
        boolean isLeader
) {
}

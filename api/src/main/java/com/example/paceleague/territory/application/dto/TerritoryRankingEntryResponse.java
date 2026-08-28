package com.example.paceleague.territory.application.dto;

import com.example.paceleague.rank.domain.enums.RankTier;

// 땅따먹기 면적 랭킹 한 줄. totalAreaSqm(총 점령 면적, m²) 큰 순으로 rank가 매겨진다.
public record TerritoryRankingEntryResponse(
        int rank,
        Long memberSno,
        String nickname,
        RankTier ownerTier,
        String ownerTierLabel,
        double totalAreaSqm,
        long territoryCount,
        boolean mine
) {
}

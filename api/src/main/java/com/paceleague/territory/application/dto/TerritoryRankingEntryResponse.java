package com.paceleague.territory.application.dto;

import com.paceleague.rank.domain.enums.RankTier;

// 땅따먹기 면적 랭킹 한 줄. totalAreaSqkm(총 점령 면적, km²) 큰 순으로 rank가 매겨진다.
// totalAreaSqm(m²)은 이전 클라이언트 호환용으로 함께 내려준다.
public record TerritoryRankingEntryResponse(
        int rank,
        Long memberSno,
        String nickname,
        RankTier ownerTier,
        String ownerTierLabel,
        double totalAreaSqkm,
        double totalAreaSqm,
        long territoryCount,
        long totalHexCount,
        boolean mine
) {
}

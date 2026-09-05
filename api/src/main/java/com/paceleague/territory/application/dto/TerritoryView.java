package com.paceleague.territory.application.dto;

import com.paceleague.rank.domain.enums.RankTier;

// 지도에 그릴 땅 하나. polygon은 [[lat,lng], ...] 위/경도 링. HP 없음(2026-09-05 제거).
public record TerritoryView(
        Long sno,
        double[][] polygon,
        double centerLat,
        double centerLng,
        String ownerNickname,
        RankTier ownerTier,
        String ownerTierLabel,
        boolean mine
) {
}

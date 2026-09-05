package com.paceleague.territory.application.dto;

import com.paceleague.rank.domain.enums.RankTier;

import java.util.List;

// 지도에 그릴 땅 하나. polygon은 [[lat,lng], ...] 위/경도 링(소유 헥사곤 합집합 외곽선). HP 없음(2026-09-05 제거).
// hexes: 개별 헥사곤 경계 링 목록 — 줌이 TerritoryProperties.hexDetailZoom 이상일 때만 채워지고,
// 그 미만이면 빈 리스트(저줌에서 지도가 무거워지는 것을 막기 위함, polygon 외곽선은 항상 채워짐).
public record TerritoryView(
        Long sno,
        double[][] polygon,
        double centerLat,
        double centerLng,
        String ownerNickname,
        RankTier ownerTier,
        String ownerTierLabel,
        boolean mine,
        List<double[][]> hexes
) {
}

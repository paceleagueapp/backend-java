package com.paceleague.territory.application.dto;

public record AdminTerritoryRankingEntry(
        int rank,
        Long memberSno,
        String nickname,
        double totalAreaSqkm,
        long territoryCount,
        long totalHexCount
) {}

package com.paceleague.territory.application.dto;

import java.util.List;

// 면적 기준 땅따먹기 랭킹. entries는 rank 오름차순(=면적 내림차순).
public record TerritoryRankingResponse(
        List<TerritoryRankingEntryResponse> entries
) {
}

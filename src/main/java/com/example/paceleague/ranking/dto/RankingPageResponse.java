package com.example.paceleague.ranking.dto;

import java.util.List;

public record RankingPageResponse(
        List<RankingUserResponse> topRanks,
        List<RankingUserResponse> aroundRanks
) {
}
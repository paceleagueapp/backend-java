package com.example.paceleague.ranking.application.dto;

import java.util.List;

public record RankingPageResponse(
        List<RankingUserResponse> topRanks,
        List<RankingUserResponse> aroundRanks
) {
}

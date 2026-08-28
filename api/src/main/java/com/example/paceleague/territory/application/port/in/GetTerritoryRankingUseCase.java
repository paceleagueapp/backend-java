package com.example.paceleague.territory.application.port.in;

import com.example.paceleague.territory.application.dto.TerritoryRankingQuery;
import com.example.paceleague.territory.application.dto.TerritoryRankingResponse;

public interface GetTerritoryRankingUseCase {
    TerritoryRankingResponse getRanking(TerritoryRankingQuery query);
}

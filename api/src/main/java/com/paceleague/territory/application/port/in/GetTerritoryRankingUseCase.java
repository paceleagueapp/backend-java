package com.paceleague.territory.application.port.in;

import com.paceleague.territory.application.dto.TerritoryRankingQuery;
import com.paceleague.territory.application.dto.TerritoryRankingResponse;

public interface GetTerritoryRankingUseCase {
    TerritoryRankingResponse getRanking(TerritoryRankingQuery query);
}

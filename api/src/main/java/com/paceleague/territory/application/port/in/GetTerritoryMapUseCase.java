package com.paceleague.territory.application.port.in;

import com.paceleague.territory.application.dto.TerritoryMapQuery;
import com.paceleague.territory.application.dto.TerritoryMapResponse;

public interface GetTerritoryMapUseCase {
    TerritoryMapResponse getMap(TerritoryMapQuery query);
}

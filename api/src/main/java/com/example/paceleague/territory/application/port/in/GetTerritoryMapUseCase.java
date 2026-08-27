package com.example.paceleague.territory.application.port.in;

import com.example.paceleague.territory.application.dto.TerritoryMapQuery;
import com.example.paceleague.territory.application.dto.TerritoryMapResponse;

public interface GetTerritoryMapUseCase {
    TerritoryMapResponse getMap(TerritoryMapQuery query);
}

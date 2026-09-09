package com.paceleague.territory.application.service;

import com.paceleague.territory.application.dto.TerritoryCaptureCount;
import com.paceleague.territory.application.port.in.shared.CountTerritoryCapturesPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TerritoryCaptureCountService implements CountTerritoryCapturesPort {

    private final TerritoryRepositoryPort territoryRepositoryPort;

    @Override
    public TerritoryCaptureCount countBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return territoryRepositoryPort.countCapturesBetween(fromInclusive, toExclusive);
    }
}

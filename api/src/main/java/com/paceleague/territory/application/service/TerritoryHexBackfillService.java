package com.paceleague.territory.application.service;

import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.paceleague.territory.domain.entity.TerritoryHex;
import com.paceleague.territory.domain.policy.H3TerritoryGrid;
import com.uber.h3core.H3Core;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// H3 도입(2026-09-05) 이전에 생성된 ACTIVE 땅을 헥사곤 집합으로 환산해 territory_hex를 채우는 1회성 백필.
// TerritoryHexBackfillRunner(ApplicationRunner)가 이 서비스를 땅 하나씩 호출한다 — 같은 클래스 안에서
// @Transactional 메서드를 셀프 호출하면 프록시를 안 타므로 별도 서비스로 분리했다.
@Service
@RequiredArgsConstructor
public class TerritoryHexBackfillService {

    private final TerritoryRepositoryPort territoryRepositoryPort;
    private final TerritoryHexRepositoryPort territoryHexRepositoryPort;
    private final TerritoryProperties props;
    private final H3Core h3Core;
    private final ObjectMapper objectMapper;

    // 땅 하나를 백필한다. 이 땅이 덮던 자리를 이미 다른 땅(먼저 백필됐거나 H3 도입 이후 새로 생긴 땅)이
    // 선점했다면 그 헥사곤은 건너뛴다(h3_index는 PK라 한 헥사곤은 항상 하나의 ACTIVE territory에만
    // 속해야 한다). 겹치지 않는 자리가 하나도 안 남으면 이 땅은 백필하지 못하고 계속 "유령 땅"으로 남는다.
    @Transactional
    public boolean backfillOne(Long territorySno) {
        Territory territory = territoryRepositoryPort.findBySno(territorySno).orElse(null);
        if (territory == null || !Territory.STATUS_ACTIVE.equals(territory.getStatus())) {
            return false;
        }

        List<double[]> ring = parseRing(territory.getPolygonJson());
        if (ring.size() < 4) {
            return false;
        }

        List<Long> covered;
        try {
            covered = H3TerritoryGrid.coverRing(h3Core, ring, props.hexResolution());
        } catch (RuntimeException e) {
            return false;
        }
        if (covered.isEmpty()) {
            return false;
        }

        Set<Long> alreadyClaimed = new HashSet<>(territoryHexRepositoryPort.findExistingIndexes(covered));
        List<Long> free = covered.stream().filter(h -> !alreadyClaimed.contains(h)).toList();
        if (free.isEmpty()) {
            return false;
        }

        List<TerritoryHex> rows = new ArrayList<>(free.size());
        for (Long h3Index : free) {
            rows.add(TerritoryHex.of(h3Index, territorySno, territory.getSeason()));
        }
        territoryHexRepositoryPort.saveAll(rows);

        double areaSqm = H3TerritoryGrid.totalAreaSqm(h3Core, free);
        List<double[]> unionRing = H3TerritoryGrid.unionBoundaryLatLng(h3Core, free);
        double[] bbox = bboxOf(unionRing);
        territory.applyHexBackfill(free.size(), areaSqm, writeRing(unionRing),
                bbox[0], bbox[1], bbox[2], bbox[3]);
        territoryRepositoryPort.save(territory);
        return true;
    }

    private List<double[]> parseRing(String polygonJson) {
        if (polygonJson == null || polygonJson.isBlank()) {
            return List.of();
        }
        try {
            double[][] arr = objectMapper.readValue(polygonJson, double[][].class);
            List<double[]> ring = new ArrayList<>(arr.length);
            for (double[] p : arr) {
                ring.add(p);
            }
            return ring;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static double[] bboxOf(List<double[]> ring) {
        double minLat = Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        for (double[] p : ring) {
            minLat = Math.min(minLat, p[0]);
            maxLat = Math.max(maxLat, p[0]);
            minLng = Math.min(minLng, p[1]);
            maxLng = Math.max(maxLng, p[1]);
        }
        return new double[]{minLat, minLng, maxLat, maxLng};
    }

    private String writeRing(List<double[]> ring) {
        double[][] arr = ring.toArray(new double[0][]);
        try {
            return objectMapper.writeValueAsString(arr);
        } catch (Exception e) {
            throw new IllegalStateException("failed to serialize territory polygon", e);
        }
    }
}

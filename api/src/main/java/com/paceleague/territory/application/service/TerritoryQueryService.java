package com.paceleague.territory.application.service;

import com.paceleague.common.i18n.Language;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.rank.application.port.in.shared.GetMemberTierPort;
import com.paceleague.rank.domain.enums.RankTier;
import com.paceleague.rank.domain.policy.RankTierLabelPolicy;
import com.paceleague.territory.application.dto.TerritoryMapQuery;
import com.paceleague.territory.application.dto.TerritoryMapResponse;
import com.paceleague.territory.application.dto.TerritoryOwnerArea;
import com.paceleague.territory.application.dto.TerritoryRankingEntryResponse;
import com.paceleague.territory.application.dto.TerritoryRankingQuery;
import com.paceleague.territory.application.dto.TerritoryRankingResponse;
import com.paceleague.territory.application.dto.TerritoryView;
import com.paceleague.territory.application.port.in.GetTerritoryMapUseCase;
import com.paceleague.territory.application.port.in.GetTerritoryRankingUseCase;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.config.TerritoryProperties;
import com.paceleague.territory.domain.entity.Territory;
import com.paceleague.territory.domain.entity.TerritoryHex;
import com.paceleague.territory.domain.policy.H3TerritoryGrid;
import com.paceleague.record.domain.policy.GeoDistanceCalculator;
import com.uber.h3core.H3Core;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TerritoryQueryService implements GetTerritoryMapUseCase, GetTerritoryRankingUseCase {

    private final TerritoryRepositoryPort territoryRepositoryPort;
    private final TerritoryHexRepositoryPort territoryHexRepositoryPort;
    private final GetMemberNicknamePort getMemberNicknamePort;
    private final GetMemberTierPort getMemberTierPort;
    private final TerritoryProperties props;
    private final H3Core h3Core;
    private final ObjectMapper objectMapper;

    @Override
    public TerritoryMapResponse getMap(TerritoryMapQuery query) {
        int minZoom = props.minZoom();
        if (query.zoom() < minZoom) {
            return new TerritoryMapResponse(true, minZoom, List.of(), List.of());
        }

        List<Territory> found = territoryRepositoryPort.findActiveIntersectingBbox(
                bd(query.swLat()), bd(query.swLng()), bd(query.neLat()), bd(query.neLng()), props.mapMaxResults());

        Language lang = Language.fromCode(query.lang());
        Map<Long, String> nicknameCache = new HashMap<>();
        Map<Long, RankTier> tierCache = new HashMap<>();
        Map<Long, List<Long>> hexIndexesByTerritory = loadHexIndexesIfDetailZoom(query.zoom(), found);

        List<TerritoryView> views = new ArrayList<>(found.size());
        for (Territory t : found) {
            Long owner = t.getOwnerMemberSno();
            String nickname = nicknameCache.computeIfAbsent(owner, getMemberNicknamePort::getNickname);
            RankTier tier = tierCache.computeIfAbsent(owner, getMemberTierPort::getTier);
            views.add(new TerritoryView(
                    t.getSno(),
                    parseRing(t.getPolygonJson()),
                    doubleOrZero(t.getCenterLat()),
                    doubleOrZero(t.getCenterLng()),
                    nickname,
                    tier,
                    RankTierLabelPolicy.label(tier, lang),
                    query.memberSno() != null && query.memberSno().equals(owner),
                    hexBoundariesOf(hexIndexesByTerritory.get(t.getSno()))
            ));
        }

        List<double[][]> emptyHexes = query.zoom() >= props.hexDetailZoom()
                ? emptyHexesInBounds(query)
                : List.of();

        return new TerritoryMapResponse(false, minZoom, views, emptyHexes);
    }

    // 아직 아무도 점령하지 않은 땅도 육각형이 보이게 — 현재 지도 bounds 전체를 덮는 H3 격자를 만들고,
    // 이미 어딘가에 배정된 셀(= territories[].hexes로 이미 그려진 것들)은 제외한다.
    private List<double[][]> emptyHexesInBounds(TerritoryMapQuery query) {
        double diagonalMeters = GeoDistanceCalculator.haversineMeters(
                query.swLat(), query.swLng(), query.neLat(), query.neLng());
        if (diagonalMeters > props.emptyHexMaxBoundsMeters()) {
            return List.of(); // bounds가 비정상적으로 넓음 — 격자 전체 계산 비용을 피하기 위해 건너뜀
        }

        List<double[]> boundsRing = List.of(
                new double[]{query.swLat(), query.swLng()},
                new double[]{query.swLat(), query.neLng()},
                new double[]{query.neLat(), query.neLng()},
                new double[]{query.neLat(), query.swLng()},
                new double[]{query.swLat(), query.swLng()});

        List<Long> covered;
        try {
            covered = H3TerritoryGrid.coverRing(h3Core, boundsRing, props.hexResolution());
        } catch (RuntimeException e) {
            return List.of();
        }
        if (covered.isEmpty()) {
            return List.of();
        }

        Set<Long> owned = new HashSet<>(territoryHexRepositoryPort.findExistingIndexes(covered));
        List<Long> empty = covered.stream()
                .filter(h -> !owned.contains(h))
                .limit(props.emptyHexMaxCells())
                .toList();
        return hexBoundariesOf(empty);
    }

    // hexDetailZoom 이상일 때만 territory_hex를 조회한다 — 저줌에서 지도가 넓게 보일 때는 헥사곤 개수가
    // 너무 많아지므로(성능/응답크기) 외곽선(polygon)만으로 충분하다.
    private Map<Long, List<Long>> loadHexIndexesIfDetailZoom(int zoom, List<Territory> found) {
        if (zoom < props.hexDetailZoom() || found.isEmpty()) {
            return Map.of();
        }
        List<Long> snos = found.stream().map(Territory::getSno).toList();
        Map<Long, List<Long>> byTerritory = new HashMap<>();
        for (TerritoryHex hex : territoryHexRepositoryPort.findByTerritorySnoIn(snos)) {
            byTerritory.computeIfAbsent(hex.getTerritorySno(), k -> new ArrayList<>()).add(hex.getH3Index());
        }
        return byTerritory;
    }

    private List<double[][]> hexBoundariesOf(List<Long> hexIndexes) {
        if (hexIndexes == null || hexIndexes.isEmpty()) {
            return List.of();
        }
        return H3TerritoryGrid.cellBoundariesLatLng(h3Core, hexIndexes).stream()
                .map(ring -> ring.toArray(new double[0][]))
                .toList();
    }

    @Override
    public TerritoryRankingResponse getRanking(TerritoryRankingQuery query) {
        List<TerritoryOwnerArea> owners = territoryRepositoryPort.findTopOwnersByArea(props.rankingMaxResults());
        Language lang = Language.fromCode(query.lang());

        List<TerritoryRankingEntryResponse> entries = new ArrayList<>(owners.size());
        int rank = 1;
        for (TerritoryOwnerArea owner : owners) {
            Long memberSno = owner.ownerMemberSno();
            RankTier tier = getMemberTierPort.getTier(memberSno);
            entries.add(new TerritoryRankingEntryResponse(
                    rank++,
                    memberSno,
                    getMemberNicknamePort.getNickname(memberSno),
                    tier,
                    RankTierLabelPolicy.label(tier, lang),
                    owner.totalAreaSqm(),
                    owner.territoryCount(),
                    query.memberSno() != null && query.memberSno().equals(memberSno)
            ));
        }
        return new TerritoryRankingResponse(entries);
    }

    private double[][] parseRing(String polygonJson) {
        if (polygonJson == null || polygonJson.isBlank()) {
            return new double[0][];
        }
        try {
            return objectMapper.readValue(polygonJson, double[][].class);
        } catch (Exception e) {
            return new double[0][];
        }
    }

    private static double doubleOrZero(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }
}

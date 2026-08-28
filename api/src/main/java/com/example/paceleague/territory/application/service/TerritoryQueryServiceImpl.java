package com.example.paceleague.territory.application.service;

import com.example.paceleague.common.i18n.Language;
import com.example.paceleague.member.application.port.in.GetMemberNicknamePort;
import com.example.paceleague.rank.application.port.in.GetMemberTierPort;
import com.example.paceleague.rank.domain.enums.RankTier;
import com.example.paceleague.rank.domain.policy.RankTierLabelPolicy;
import com.example.paceleague.territory.application.dto.TerritoryMapQuery;
import com.example.paceleague.territory.application.dto.TerritoryMapResponse;
import com.example.paceleague.territory.application.dto.TerritoryOwnerArea;
import com.example.paceleague.territory.application.dto.TerritoryRankingEntryResponse;
import com.example.paceleague.territory.application.dto.TerritoryRankingQuery;
import com.example.paceleague.territory.application.dto.TerritoryRankingResponse;
import com.example.paceleague.territory.application.dto.TerritoryView;
import com.example.paceleague.territory.application.port.in.GetTerritoryMapUseCase;
import com.example.paceleague.territory.application.port.in.GetTerritoryRankingUseCase;
import com.example.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.example.paceleague.territory.config.TerritoryProperties;
import com.example.paceleague.territory.domain.entity.Territory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TerritoryQueryServiceImpl implements GetTerritoryMapUseCase, GetTerritoryRankingUseCase {

    private final TerritoryRepositoryPort territoryRepositoryPort;
    private final GetMemberNicknamePort getMemberNicknamePort;
    private final GetMemberTierPort getMemberTierPort;
    private final TerritoryProperties props;
    private final ObjectMapper objectMapper;

    @Override
    public TerritoryMapResponse getMap(TerritoryMapQuery query) {
        int minZoom = props.minZoom();
        if (query.zoom() < minZoom) {
            return new TerritoryMapResponse(true, minZoom, List.of());
        }

        List<Territory> found = territoryRepositoryPort.findActiveIntersectingBbox(
                bd(query.swLat()), bd(query.swLng()), bd(query.neLat()), bd(query.neLng()), props.mapMaxResults());

        Language lang = Language.fromCode(query.lang());
        Map<Long, String> nicknameCache = new HashMap<>();
        Map<Long, RankTier> tierCache = new HashMap<>();

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
                    t.getHp(),
                    t.getMaxHp(),
                    query.memberSno() != null && query.memberSno().equals(owner)
            ));
        }
        return new TerritoryMapResponse(false, minZoom, views);
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

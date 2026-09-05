package com.paceleague.territory.application.dto;

import java.util.List;

// zoomTooLow=true 이면 territories/emptyHexes는 항상 빈 목록이며, 클라이언트는 "지도를 더 확대하세요" 안내를 보여준다.
// emptyHexes: 아직 아무도 점령하지 않은 H3 셀의 경계 링 목록 — 소유된 셀은 territories[].hexes로 이미
// 그려지므로 여기엔 포함되지 않는다. TerritoryProperties.hexDetailZoom 미만이면 항상 빈 리스트.
public record TerritoryMapResponse(
        boolean zoomTooLow,
        int minZoom,
        List<TerritoryView> territories,
        List<double[][]> emptyHexes
) {
}

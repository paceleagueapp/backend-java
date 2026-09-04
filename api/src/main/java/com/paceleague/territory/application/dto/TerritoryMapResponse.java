package com.paceleague.territory.application.dto;

import java.util.List;

// zoomTooLow=true 이면 territories는 항상 빈 목록이며, 클라이언트는 "지도를 더 확대하세요" 안내를 보여준다.
public record TerritoryMapResponse(
        boolean zoomTooLow,
        int minZoom,
        List<TerritoryView> territories
) {
}

package com.paceleague.territory.application.dto;

// 지도 화면이 보고 있는 영역(bounds)과 줌 레벨로 땅 목록을 조회하는 질의.
// zoom이 임계값 미만이면 서버는 빈 목록 + zoomTooLow=true를 돌려준다(데이터 과다 방지).
public record TerritoryMapQuery(
        double swLat,
        double swLng,
        double neLat,
        double neLng,
        int zoom,
        String lang,
        Long memberSno
) {
}

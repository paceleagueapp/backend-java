package com.paceleague.territory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// paceleague.territory.* 튜닝 값 묶음. app.jwt(JwtProperties)와 같은 방식 —
// 관련 프로퍼티가 여러 개 뭉쳐 있어 @Value 대신 @ConfigurationProperties로 묶는다.
// application*.yml에는 넣지 않고 아래 기본값을 그대로 쓴다(운영에서 필요 시 env로 덮어씀).
// 2026-09-05: HP/공격력/회복력/기여도 윈도우 제거 — 겹치면 무조건 즉시 점령으로 단순화.
@ConfigurationProperties(prefix = "paceleague.territory")
public record TerritoryProperties(
        Integer minZoom,
        Integer mapMaxResults,
        Double closeThresholdMeters,
        Double minPerimeterMeters,
        Double minAreaSqm,
        Double maxAreaSqm,
        Integer rankingMaxResults,
        Integer hexResolution,
        Integer hexDetailZoom,
        Integer emptyHexMaxCells,
        Double emptyHexMaxBoundsMeters
) {
    public TerritoryProperties {
        if (minZoom == null) minZoom = 13;
        if (mapMaxResults == null) mapMaxResults = 300;
        if (rankingMaxResults == null) rankingMaxResults = 100;
        if (closeThresholdMeters == null) closeThresholdMeters = 50.0;
        if (minPerimeterMeters == null) minPerimeterMeters = 300.0;
        if (minAreaSqm == null) minAreaSqm = 10_000.0;
        if (maxAreaSqm == null) maxAreaSqm = 5_000_000.0;
        if (hexResolution == null) hexResolution = 12; // H3 res12 평균 ~307㎡ — 소유권/충돌 판정의 최소 단위
        // 이 줌 이상에서만 GET /api/territory/map이 개별 헥사곤 경계도 함께 내려준다(저줌에서는 외곽선만).
        if (hexDetailZoom == null) hexDetailZoom = 17;
        // 미점령 셀 격자(emptyHexes) 응답 개수 상한 — hexDetailZoom 자체가 이미 화면을 좁혀주지만,
        // 넓은 화면/이상한 bounds 요청에서도 한 응답이 과도하게 커지지 않도록 하는 안전장치.
        if (emptyHexMaxCells == null) emptyHexMaxCells = 4000;
        // GET /api/territory/map은 공개 API라 zoom과 무관하게 임의로 넓은 swLat/swLng/neLat/neLng을
        // 보낼 수 있다 — emptyHexMaxCells는 "잘라서 반환하는" 상한일 뿐, H3 격자 전체를 계산하는 비용
        // 자체는 못 막는다(수십만 셀도 만들어질 수 있음). 대각선이 이 값(m)을 넘으면 emptyHexes 계산을
        // 아예 건너뛴다 — 정상적인 줌 17+ 뷰포트는 보통 수백 m 수준이라 실사용엔 영향 없다.
        if (emptyHexMaxBoundsMeters == null) emptyHexMaxBoundsMeters = 3000.0;
    }
}

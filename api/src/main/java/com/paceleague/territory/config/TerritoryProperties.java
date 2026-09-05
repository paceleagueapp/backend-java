package com.paceleague.territory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// paceleague.territory.* 튜닝 값 묶음. app.jwt(JwtProperties)와 같은 방식 —
// 관련 프로퍼티가 여러 개 뭉쳐 있어 @Value 대신 @ConfigurationProperties로 묶는다.
// application*.yml에는 넣지 않고 아래 기본값을 그대로 쓴다(운영에서 필요 시 env로 덮어씀).
@ConfigurationProperties(prefix = "paceleague.territory")
public record TerritoryProperties(
        Integer minZoom,
        Integer mapMaxResults,
        Double closeThresholdMeters,
        Double minPerimeterMeters,
        Double minAreaSqm,
        Double maxAreaSqm,
        Integer defaultMaxHp,
        Double attackFactor,
        Double healFactor,
        Integer contributionWindowMinutes,
        Integer rankingMaxResults,
        Integer hexResolution
) {
    public TerritoryProperties {
        if (minZoom == null) minZoom = 13;
        if (mapMaxResults == null) mapMaxResults = 300;
        if (rankingMaxResults == null) rankingMaxResults = 100;
        if (closeThresholdMeters == null) closeThresholdMeters = 50.0;
        if (minPerimeterMeters == null) minPerimeterMeters = 300.0;
        if (minAreaSqm == null) minAreaSqm = 10_000.0;
        if (maxAreaSqm == null) maxAreaSqm = 5_000_000.0;
        if (defaultMaxHp == null) defaultMaxHp = 100;
        if (attackFactor == null) attackFactor = 0.5;
        if (healFactor == null) healFactor = 0.5;
        if (contributionWindowMinutes == null) contributionWindowMinutes = 60;
        if (hexResolution == null) hexResolution = 12; // H3 res12 평균 ~307㎡ — 소유권/충돌 판정의 최소 단위
    }
}

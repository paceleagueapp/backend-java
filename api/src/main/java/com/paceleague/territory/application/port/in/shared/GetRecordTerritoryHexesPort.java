package com.paceleague.territory.application.port.in.shared;

import java.util.List;

// record 도메인(GET /api/record/{recordSno}/gps)이 그 러닝으로 얻은 땅의 헥사곤 경계를 조회하는 진입 포트.
// territory.source_record_sno 로 연결된 ACTIVE 땅의 "현재 남아 있는" 헥사곤만 — 이후 뺏긴 셀은 빠진다.
// 링 형식은 지도 API 의 territories[].hexes 와 동일([[lat, lng], ...] 닫힌 링의 목록).
public interface GetRecordTerritoryHexesPort {
    List<double[][]> getHexBoundaries(Long recordSno);
}

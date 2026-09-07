package com.paceleague.record.application.dto;

import java.time.LocalDateTime;
import java.util.List;

// territory 재생(historical replay) 전용 — territory_mode로 완주한 세션 1건을 ProcessTerritoryRunCommand로
// 바로 넘길 수 있는 형태로 미리 파싱해 둔 것. coords는 record_track.points_json(GpsPoint 목록)에서
// 위/경도만 뽑은 [lat, lng] 목록 — SaveGpsSessionService.claimTerritoryBestEffort가 실시간 처리 시
// 만드는 것과 동일한 형태.
public record TerritoryRunHistoryEntry(
        Long memberSno,
        Long recordSno,
        Long trackSno,
        List<double[]> coords,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}

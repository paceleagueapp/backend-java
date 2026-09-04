package com.paceleague.territory.application.dto;

import java.time.LocalDateTime;
import java.util.List;

// 땅따먹기 모드로 끝난 러닝 1건을 territory 도메인에 넘기는 커맨드.
// coords: [[lat,lng], ...] 러닝 전체 경로(시간순). record.record_track.points_json에서 뽑아 넘긴다.
public record ProcessTerritoryRunCommand(
        Long memberSno,
        Long recordSno,
        Long trackSno,
        List<double[]> coords,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}

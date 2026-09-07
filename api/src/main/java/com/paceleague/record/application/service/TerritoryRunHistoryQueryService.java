package com.paceleague.record.application.service;

import com.paceleague.record.application.dto.GpsSessionRequest.GpsPoint;
import com.paceleague.record.application.dto.TerritoryRunHistoryEntry;
import com.paceleague.record.application.port.in.shared.GetTerritoryRunHistoryPort;
import com.paceleague.record.application.port.out.RecordTrackRepositoryPort;
import com.paceleague.record.domain.entity.RecordTrack;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// territory 재생(historical replay, 2026-09-07) 전용 조회. points_json → coords 파싱은
// SaveGpsSessionService.claimTerritoryBestEffort가 실시간 처리 때 하는 것과 동일한 로직 —
// 딱 한 번 쓰고 끝날 배치용 포트 하나 때문에 별도 유틸로 뽑지는 않았다.
@Service
@RequiredArgsConstructor
public class TerritoryRunHistoryQueryService implements GetTerritoryRunHistoryPort {

    private static final Logger log = LoggerFactory.getLogger(TerritoryRunHistoryQueryService.class);

    private final RecordTrackRepositoryPort recordTrackRepositoryPort;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findFinishedTerritoryModeTrackSnosOrderByEndedAt() {
        return recordTrackRepositoryPort.findFinishedTerritoryModeSnosOrderByEndedAt();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TerritoryRunHistoryEntry> getEntry(Long trackSno) {
        RecordTrack track = recordTrackRepositoryPort.findBySno(trackSno).orElse(null);
        if (track == null || !track.isFinished()) {
            return Optional.empty();
        }
        try {
            List<GpsPoint> points = objectMapper.readValue(track.getPointsJson(), new TypeReference<List<GpsPoint>>() {
            });
            List<double[]> coords = points.stream()
                    .filter(p -> p.latitude() != null && p.longitude() != null)
                    .map(p -> new double[]{p.latitude(), p.longitude()})
                    .toList();
            return Optional.of(new TerritoryRunHistoryEntry(
                    track.getUno(), track.getRecordSno(), track.getSno(), coords,
                    track.getStartedAt(), track.getEndedAt()));
        } catch (Exception e) {
            log.warn("territory 재생: trackSno={} points_json 파싱 실패, 건너뜀 — err={}", trackSno, e.toString());
            return Optional.empty();
        }
    }
}

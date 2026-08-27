package com.example.paceleague.record.application.service;

import com.example.paceleague.record.application.dto.GpsSessionRequest;
import com.example.paceleague.record.application.dto.GpsSessionRequest.GpsPoint;
import com.example.paceleague.record.application.dto.GpsSessionResponse;
import com.example.paceleague.record.application.dto.RecordCreateRequest;
import com.example.paceleague.record.application.port.in.RecordService;
import com.example.paceleague.record.application.port.in.SaveGpsSessionUseCase;
import com.example.paceleague.record.application.port.out.RecordTrackRepositoryPort;
import com.example.paceleague.record.domain.entity.RecordTrack;
import com.example.paceleague.record.domain.policy.GeoDistanceCalculator;
import com.example.paceleague.record.domain.policy.GpsSessionValidator;
import com.example.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.example.paceleague.territory.application.port.in.ProcessTerritoryRunUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SaveGpsSessionServiceImpl implements SaveGpsSessionUseCase {

    private static final Logger log = LoggerFactory.getLogger(SaveGpsSessionServiceImpl.class);

    private final RecordService recordService;
    private final RecordTrackRepositoryPort recordTrackRepositoryPort;
    private final ProcessTerritoryRunUseCase processTerritoryRunUseCase;
    private final ObjectMapper objectMapper;

    public SaveGpsSessionServiceImpl(RecordService recordService,
                                     RecordTrackRepositoryPort recordTrackRepositoryPort,
                                     ProcessTerritoryRunUseCase processTerritoryRunUseCase,
                                     ObjectMapper objectMapper) {
        this.recordService = recordService;
        this.recordTrackRepositoryPort = recordTrackRepositoryPort;
        this.processTerritoryRunUseCase = processTerritoryRunUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public GpsSessionResponse ingest(Long uno, GpsSessionRequest req) {
        GpsSessionValidator.validate(uno, req);

        boolean finished = Boolean.TRUE.equals(req.finished());
        List<GpsPoint> incoming = sortByTime(req.points());

        RecordTrack session = recordTrackRepositoryPort
                .findByUnoAndClientRunId(uno, req.clientRunId())
                .orElse(null);

        // 이미 종료된 세션에 또 청크가 오면(재전송 등) 아무것도 하지 않고 확정된 결과만 돌려준다.
        if (session != null && session.isFinished()) {
            return summary(session, 0, incoming.size());
        }

        if (session == null) {
            if (incoming.isEmpty()) {
                throw new IllegalArgumentException("no GPS points to start a session");
            }
            session = recordTrackRepositoryPort.save(newSession(uno, req, incoming.get(0)));
        }

        int accepted = 0;
        int skipped = 0;
        if (!incoming.isEmpty()) {
            LocalDateTime watermark = session.getLastPointAt();
            List<GpsPoint> kept = new ArrayList<>();
            for (GpsPoint p : incoming) {
                if (watermark == null || toUtc(p.recordedAt()).isAfter(watermark)) {
                    kept.add(p);
                } else {
                    skipped++;
                }
            }
            if (session.getPointCount() + kept.size() > GpsSessionValidator.MAX_SESSION_POINTS) {
                throw new IllegalArgumentException("session exceeds max points (" + GpsSessionValidator.MAX_SESSION_POINTS + ")");
            }
            if (!kept.isEmpty()) {
                applyChunk(session, kept);
                accepted = kept.size();
            }
        }

        Long recordSno = session.getRecordSno();
        if (finished) {
            recordSno = finalizeRun(uno, session);
        }
        recordTrackRepositoryPort.save(session);

        return summary(session, accepted, skipped);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findIdleActiveSessionSnos(Duration idleFor, int limit) {
        return recordTrackRepositoryPort.findIdleActiveSessionSnos(LocalDateTime.now().minus(idleFor), limit);
    }

    @Override
    @Transactional
    public void finalizeSession(Long trackSno) {
        RecordTrack session = recordTrackRepositoryPort.findBySno(trackSno).orElse(null);
        if (session == null || !session.isActive()) {
            return; // 그 사이 청크가 finished로 마감했거나(FINISHED) 이미 폐기됨(ABANDONED)
        }
        finalizeRun(session.getUno(), session);
        recordTrackRepositoryPort.save(session);
    }

    @Override
    @Transactional
    public void abandonSession(Long trackSno) {
        recordTrackRepositoryPort.findBySno(trackSno)
                .filter(RecordTrack::isActive)
                .ifPresent(session -> {
                    session.abandon();
                    recordTrackRepositoryPort.save(session);
                });
    }

    private void applyChunk(RecordTrack session, List<GpsPoint> kept) {
        double added = 0;
        Double prevLat = session.getLastLat() == null ? null : session.getLastLat().doubleValue();
        Double prevLng = session.getLastLng() == null ? null : session.getLastLng().doubleValue();
        for (GpsPoint p : kept) {
            if (prevLat != null) {
                added += GeoDistanceCalculator.haversineMeters(prevLat, prevLng, p.latitude(), p.longitude());
            }
            prevLat = p.latitude();
            prevLng = p.longitude();
        }

        GpsPoint last = kept.get(kept.size() - 1);
        session.appendChunk(
                kept.size(),
                toUtc(last.recordedAt()),
                BigDecimal.valueOf(last.latitude()),
                BigDecimal.valueOf(last.longitude()),
                BigDecimal.valueOf(added),
                mergePointsJson(session.getPointsJson(), kept)
        );
    }

    private Long finalizeRun(Long uno, RecordTrack session) {
        if (session.getPointCount() == null || session.getPointCount() == 0) {
            throw new IllegalArgumentException("cannot finish a run with no GPS data");
        }
        // record 생성 + 시즌/점수 산정은 기존 단건 저장 로직을 그대로 재사용(거리·페이스 상한 검증 포함).
        Long recordSno = recordService.create(
                uno,
                new RecordCreateRequest(session.getDistanceMeters(), session.getStartedAt(),
                        session.getEndedAt(), session.getUtcOffset())
        );
        session.finish(recordSno);

        // 땅따먹기 모드로 시작한 세션만 땅 판정 대상. 판정은 best-effort —
        // 실패해도 러닝 기록/점수는 그대로 유지된다(ProcessTerritoryRunUseCase는 REQUIRES_NEW 트랜잭션).
        if (session.isTerritoryMode()) {
            claimTerritoryBestEffort(uno, session, recordSno);
        }
        return recordSno;
    }

    private void claimTerritoryBestEffort(Long uno, RecordTrack session, Long recordSno) {
        try {
            List<GpsPoint> points = objectMapper.readValue(
                    session.getPointsJson(), new TypeReference<List<GpsPoint>>() {});
            List<double[]> coords = points.stream()
                    .filter(p -> p.latitude() != null && p.longitude() != null)
                    .map(p -> new double[]{p.latitude(), p.longitude()})
                    .toList();
            processTerritoryRunUseCase.process(new ProcessTerritoryRunCommand(
                    uno, recordSno, session.getSno(), coords,
                    session.getStartedAt(), session.getEndedAt()));
        } catch (Exception e) {
            log.warn("territory 처리 실패 — 러닝 기록은 정상 저장됨. trackSno={}, err={}",
                    session.getSno(), e.toString());
        }
    }

    private RecordTrack newSession(Long uno, GpsSessionRequest req, GpsPoint firstPoint) {
        GpsSessionRequest.Location loc = req.location();
        GpsSessionRequest.Device dev = req.device();
        return RecordTrack.builder()
                .uno(uno)
                .clientRunId(req.clientRunId())
                .activityType(req.activityType() == null ? "RUNNING" : req.activityType())
                .territoryMode(Boolean.TRUE.equals(req.territoryMode()))
                .schemaVersion(req.schemaVersion())
                .utcOffset(req.utcOffset())
                .startedAt(toUtc(firstPoint.recordedAt()))
                .locRequestedIntervalMs(loc == null ? null : loc.requestedIntervalMs())
                .locDistanceFilterMeters(loc == null ? null : loc.distanceFilterMeters())
                .locAlgorithmVersion(loc == null ? null : loc.algorithmVersion())
                .devicePlatform(dev == null ? null : dev.platform())
                .deviceAppVersion(dev == null ? null : dev.appVersion())
                .deviceAppBuildNumber(dev == null ? null : dev.appBuildNumber())
                .build();
    }

    private static List<GpsPoint> sortByTime(List<GpsPoint> points) {
        if (points == null || points.isEmpty()) {
            return List.of();
        }
        return points.stream()
                .sorted(Comparator.comparing(GpsPoint::recordedAt))
                .toList();
    }

    private static LocalDateTime toUtc(OffsetDateTime odt) {
        return odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private String mergePointsJson(String existingJson, List<GpsPoint> kept) {
        try {
            List<GpsPoint> all = (existingJson == null || existingJson.isBlank())
                    ? new ArrayList<>()
                    : objectMapper.readValue(existingJson, new TypeReference<List<GpsPoint>>() {});
            all.addAll(kept);
            return objectMapper.writeValueAsString(all);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to merge GPS points", e);
        }
    }

    private static GpsSessionResponse summary(RecordTrack s, int accepted, int skipped) {
        return new GpsSessionResponse(
                s.getClientRunId(),
                s.getStatus(),
                s.getChunkCount() == null ? 0 : s.getChunkCount(),
                accepted,
                skipped,
                s.getPointCount() == null ? 0 : s.getPointCount(),
                s.getDistanceMeters(),
                s.getRecordSno()
        );
    }
}

package com.example.paceleague.record.application.service;

import com.example.paceleague.record.application.dto.GpsSessionRequest;
import com.example.paceleague.record.application.dto.GpsSessionResponse;
import com.example.paceleague.record.application.dto.RecordCreateRequest;
import com.example.paceleague.record.application.port.in.RecordService;
import com.example.paceleague.record.application.port.in.SaveGpsSessionUseCase;
import com.example.paceleague.record.application.port.out.RecordTrackRepositoryPort;
import com.example.paceleague.record.domain.entity.RecordTrack;
import com.example.paceleague.record.domain.policy.GpsSessionValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class SaveGpsSessionServiceImpl implements SaveGpsSessionUseCase {

    private final RecordService recordService;
    private final RecordTrackRepositoryPort recordTrackRepositoryPort;
    private final ObjectMapper objectMapper;

    public SaveGpsSessionServiceImpl(RecordService recordService,
                                     RecordTrackRepositoryPort recordTrackRepositoryPort,
                                     ObjectMapper objectMapper) {
        this.recordService = recordService;
        this.recordTrackRepositoryPort = recordTrackRepositoryPort;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public GpsSessionResponse save(Long uno, GpsSessionRequest req) {
        GpsSessionValidator.validate(uno, req);

        // 멱등 처리: 같은 clientRunId가 이미 저장돼 있으면 record를 새로 만들지 않고 기존 결과를 반환.
        // (동시에 같은 요청이 두 번 들어오는 드문 레이스에서는 client_run_id UNIQUE 제약에 걸려 뒤 요청이
        //  실패하지만, 앱이 재시도하면 이 분기에서 걸러진다.)
        return recordTrackRepositoryPort.findByUnoAndClientRunId(uno, req.clientRunId())
                .map(existing -> new GpsSessionResponse(existing.getRecordSno(), existing.getSno(), true))
                .orElseGet(() -> createRecordAndTrack(uno, req));
    }

    private GpsSessionResponse createRecordAndTrack(Long uno, GpsSessionRequest req) {
        LocalDateTime startTime = toUtcLocalDateTime(req.startedAt());
        LocalDateTime endTime = toUtcLocalDateTime(req.endedAt());

        // record 생성 + 시즌/점수 산정은 기존 단건 저장 로직을 그대로 재사용(거리·페이스 상한 검증 포함).
        Long recordSno = recordService.create(
                uno,
                new RecordCreateRequest(req.distanceMeters(), startTime, endTime, req.utcOffset())
        );

        RecordTrack saved = recordTrackRepositoryPort.save(toTrack(uno, recordSno, req, startTime, endTime));

        return new GpsSessionResponse(recordSno, saved.getSno(), false);
    }

    private RecordTrack toTrack(Long uno, Long recordSno, GpsSessionRequest req,
                               LocalDateTime startedAt, LocalDateTime endedAt) {
        GpsSessionRequest.Location loc = req.location();
        GpsSessionRequest.Device dev = req.device();
        return RecordTrack.builder()
                .uno(uno)
                .recordSno(recordSno)
                .clientRunId(req.clientRunId())
                .schemaVersion(req.schemaVersion())
                .activityType(req.activityType())
                .status(req.status())
                .startedAt(startedAt)
                .endedAt(endedAt)
                .elapsedDurationMs(req.elapsedDurationMs())
                .distanceMeters(req.distanceMeters())
                .pointCount(req.pointCount())
                .locRequestedIntervalMs(loc == null ? null : loc.requestedIntervalMs())
                .locDistanceFilterMeters(loc == null ? null : loc.distanceFilterMeters())
                .locAlgorithmVersion(loc == null ? null : loc.algorithmVersion())
                .devicePlatform(dev == null ? null : dev.platform())
                .deviceAppVersion(dev == null ? null : dev.appVersion())
                .deviceAppBuildNumber(dev == null ? null : dev.appBuildNumber())
                .pointsJson(serializePoints(req))
                .build();
    }

    private static LocalDateTime toUtcLocalDateTime(java.time.OffsetDateTime odt) {
        return odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private String serializePoints(GpsSessionRequest req) {
        try {
            return objectMapper.writeValueAsString(req.points());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to serialize GPS points", e);
        }
    }
}

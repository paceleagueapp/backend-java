package com.paceleague.record.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paceleague.record.application.dto.GpsSessionRequest.GpsPoint;
import com.paceleague.record.application.dto.RecordGpsTrackResponse;
import com.paceleague.record.application.dto.RecordListItemResponse;
import com.paceleague.record.application.dto.RecordMonthResponse;
import com.paceleague.record.application.dto.RecordResponse;
import com.paceleague.record.application.dto.RecordSummaryDto;
import com.paceleague.record.application.dto.RecordSummaryProjection;
import com.paceleague.record.application.dto.RunningRecordResponse;
import com.paceleague.record.application.port.in.shared.GetRecordSummaryPort;
import com.paceleague.record.application.port.in.RecordQueryUseCase;
import com.paceleague.record.application.port.out.RecordRepositoryPort;
import com.paceleague.record.application.port.out.RecordTrackRepositoryPort;
import com.paceleague.record.domain.entity.Record;
import com.paceleague.record.domain.entity.RecordTrack;
import com.paceleague.record.domain.policy.RecordSummaryCalculator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class RecordQueryService implements RecordQueryUseCase, GetRecordSummaryPort {
    private final RecordRepositoryPort recordRepositoryPort;
    private final RecordTrackRepositoryPort recordTrackRepositoryPort;
    private final ObjectMapper objectMapper;

    public RecordQueryService(RecordRepositoryPort recordRepositoryPort,
                              RecordTrackRepositoryPort recordTrackRepositoryPort,
                              ObjectMapper objectMapper) {
        this.recordRepositoryPort = recordRepositoryPort;
        this.recordTrackRepositoryPort = recordTrackRepositoryPort;
        this.objectMapper = objectMapper;
    }

    public Record getOne(Long uno, Long sno) {
        return recordRepositoryPort.findBySnoAndUno(sno, uno)
                .orElseThrow(() -> new IllegalArgumentException("record not found"));
    }

    public Page<Record> getPage(Long uno, int page, int size) {
        int pageSize = (size <= 0) ? 10 : size; // 기본 10
        var pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "startTime")
        );
        return recordRepositoryPort.findByUnoOrderByStartTimeDesc(uno, pageable);
    }

    public RecordMonthResponse getMonthAll(Long uno, int year, int month, BigDecimal weightKg) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be 1~12");
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        RecordSummaryProjection memberProjection = recordRepositoryPort.findMemberSummary(uno);
        RecordSummaryProjection monthProjection = recordRepositoryPort.findMonthSummary(uno, from, to);

        RecordSummaryDto memberSummary = RecordSummaryCalculator.from(memberProjection, weightKg);
        RecordSummaryDto monthSummary = RecordSummaryCalculator.from(monthProjection, weightKg);

        List<RecordResponse> monthRecords =
                recordRepositoryPort.findByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
                        uno, from, to
                ).stream()
                        .map(RecordResponse::from)
                        .toList();

        return new RecordMonthResponse(memberSummary, monthSummary, monthRecords);
    }

    public List<RunningRecordResponse> getRecent30DaysRecords(Long uno) {
        LocalDateTime endDateTime = LocalDateTime.now();
        LocalDateTime startDateTime = endDateTime.minusDays(30);

        List<Record> records = recordRepositoryPort
                .findByUnoAndStartTimeBetweenOrderByStartTimeDesc(
                        uno,
                        startDateTime,
                        endDateTime
                );

        return records.stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<RunningRecordResponse> getSummary(Long recordSno) {
        return recordRepositoryPort.findBySno(recordSno).map(this::toResponse);
    }

    public List<RecordListItemResponse> listMyRecords(Long uno) {
        Set<Long> recordSnosWithTrack = new HashSet<>(recordTrackRepositoryPort.findRecordSnosByUno(uno));
        return recordRepositoryPort.findByUnoOrderByStartTimeDesc(uno).stream()
                .map(r -> new RecordListItemResponse(
                        r.getSno(),
                        r.getDistanceRecord(),
                        r.getStartTime(),
                        r.getEndTime(),
                        r.getCreateAt(),
                        recordSnosWithTrack.contains(r.getSno())
                ))
                .toList();
    }

    public RecordGpsTrackResponse getGpsTrack(Long uno, Long recordSno) {
        // 본인 소유 러닝인지 먼저 검증 (없거나 남의 것이면 400)
        recordRepositoryPort.findBySnoAndUno(recordSno, uno)
                .orElseThrow(() -> new IllegalArgumentException("record not found"));

        RecordTrack track = recordTrackRepositoryPort.findByRecordSno(recordSno)
                .orElseThrow(() -> new IllegalArgumentException("이 러닝에는 GPS 트랙이 없습니다."));

        List<GpsPoint> points = parsePoints(track);
        return new RecordGpsTrackResponse(
                recordSno,
                track.getSno(),
                track.getStatus(),
                track.isTerritoryMode(),
                track.getStartedAt(),
                track.getEndedAt(),
                track.getDistanceMeters(),
                track.getPointCount() == null ? points.size() : track.getPointCount(),
                points
        );
    }

    private List<GpsPoint> parsePoints(RecordTrack track) {
        String json = track.getPointsJson();
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<GpsPoint>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to parse GPS points for track " + track.getSno(), e);
        }
    }

    private RunningRecordResponse toResponse(Record record) {
        return RunningRecordResponse.builder()
                .recordSno(record.getSno())
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .distance(record.getDistanceRecord())
                .createAt(record.getCreateAt())
                .build();
    }
}

package com.paceleague.record.application.service;

import com.paceleague.record.application.dto.RecordMonthResponse;
import com.paceleague.record.application.dto.RecordResponse;
import com.paceleague.record.application.dto.RecordSummaryDto;
import com.paceleague.record.application.dto.RecordSummaryProjection;
import com.paceleague.record.application.dto.RunningRecordResponse;
import com.paceleague.record.application.port.in.GetRecordSummaryPort;
import com.paceleague.record.application.port.in.RecordQueryService;
import com.paceleague.record.application.port.out.RecordRepositoryPort;
import com.paceleague.record.domain.entity.Record;
import com.paceleague.record.domain.policy.RecordSummaryCalculator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RecordQueryServiceImpl implements RecordQueryService, GetRecordSummaryPort {
    private final RecordRepositoryPort recordRepositoryPort;

    public RecordQueryServiceImpl(RecordRepositoryPort recordRepositoryPort) {
        this.recordRepositoryPort = recordRepositoryPort;
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

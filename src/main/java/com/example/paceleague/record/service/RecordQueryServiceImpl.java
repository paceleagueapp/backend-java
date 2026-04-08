package com.example.paceleague.record.service;

import com.example.paceleague.record.dto.RecordMonthResponse;
import com.example.paceleague.record.dto.RecordSummaryDto;
import com.example.paceleague.record.dto.RecordSummaryProjection;
import com.example.paceleague.record.repository.RecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.paceleague.record.entity.Record;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecordQueryServiceImpl implements RecordQueryService{
    private final RecordRepository recordRepository;

    public RecordQueryServiceImpl(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public Record getOne(Long uno, Long sno) {
        return recordRepository.findBySnoAndUno(sno, uno)
                .orElseThrow(() -> new IllegalArgumentException("record not found"));
    }

    public Page<Record> getPage(Long uno, int page, int size) {
        int pageSize = (size <= 0) ? 10 : size; // 기본 10
        var pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "startTime")
        );
        return recordRepository.findByUnoOrderByStartTimeDesc(uno, pageable);
    }

    public RecordMonthResponse getMonthAll(Long uno, int year, int month, BigDecimal weightKg) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be 1~12");
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        RecordSummaryProjection memberProjection = recordRepository.findMemberSummary(uno);
        RecordSummaryProjection monthProjection = recordRepository.findMonthSummary(uno, from, to);

        RecordSummaryDto memberSummary = RecordSummaryCalculator.from(memberProjection, weightKg);
        RecordSummaryDto monthSummary = RecordSummaryCalculator.from(monthProjection, weightKg);

        List<Record> monthRecords =
                recordRepository.findByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
                        uno, from, to
                );

        return new RecordMonthResponse(memberSummary, monthSummary, monthRecords);
    }
}

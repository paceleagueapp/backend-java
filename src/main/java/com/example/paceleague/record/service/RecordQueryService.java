package com.example.paceleague.record.service;

import com.example.paceleague.record.repository.RecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.paceleague.record.entity.Record;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class RecordQueryService {
    private final RecordRepository recordRepository;

    public RecordQueryService(RecordRepository recordRepository) {
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

    public List<Record> getMonthAll(Long uno, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be 1~12");
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay(); // [from, to)
        return recordRepository.findByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(uno, from, to);
    }
}

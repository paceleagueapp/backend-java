package com.paceleague.record.adapter.out.persistence;

import com.paceleague.record.application.dto.RecordSummaryProjection;
import com.paceleague.record.application.port.out.RecordRepositoryPort;
import com.paceleague.record.domain.entity.Record;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecordPersistenceAdapter implements RecordRepositoryPort {

    private final RecordJpaRepository recordJpaRepository;

    public Record save(Record record) {
        return recordJpaRepository.save(record);
    }

    public Optional<Record> findBySnoAndUno(Long sno, Long uno) {
        return recordJpaRepository.findBySnoAndUno(sno, uno);
    }

    public Optional<Record> findBySno(Long sno) {
        return recordJpaRepository.findById(sno);
    }

    public Page<Record> findByUnoOrderByStartTimeDesc(Long uno, Pageable pageable) {
        return recordJpaRepository.findByUnoOrderByStartTimeDesc(uno, pageable);
    }

    public List<Record> findByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            Long uno, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return recordJpaRepository.findByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(uno, fromInclusive, toExclusive);
    }

    public RecordSummaryProjection findMemberSummary(Long uno) {
        return recordJpaRepository.findMemberSummary(uno);
    }

    public RecordSummaryProjection findMonthSummary(Long uno, LocalDateTime fromDt, LocalDateTime toDt) {
        return recordJpaRepository.findMonthSummary(uno, fromDt, toDt);
    }

    public long countByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long uno, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return recordJpaRepository.countByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThan(uno, fromInclusive, toExclusive);
    }

    public List<Record> findByUnoAndStartTimeBetweenOrderByStartTimeDesc(
            Long uno, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return recordJpaRepository.findByUnoAndStartTimeBetweenOrderByStartTimeDesc(uno, startDateTime, endDateTime);
    }

    public List<Record> findByUnoAndStartTimeBetween(Long uno, LocalDateTime from, LocalDateTime to) {
        return recordJpaRepository.findByUnoAndStartTimeBetween(uno, from, to);
    }
}

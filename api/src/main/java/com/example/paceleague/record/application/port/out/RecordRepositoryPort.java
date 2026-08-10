package com.example.paceleague.record.application.port.out;

import com.example.paceleague.record.application.dto.RecordSummaryProjection;
import com.example.paceleague.record.domain.entity.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordRepositoryPort {
    Record save(Record record);

    Optional<Record> findBySnoAndUno(Long sno, Long uno);

    Page<Record> findByUnoOrderByStartTimeDesc(Long uno, Pageable pageable);

    List<Record> findByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            Long uno,
            LocalDateTime fromInclusive,
            LocalDateTime toExclusive
    );

    RecordSummaryProjection findMemberSummary(Long uno);

    RecordSummaryProjection findMonthSummary(Long uno, LocalDateTime fromDt, LocalDateTime toDt);

    long countByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long uno, LocalDateTime fromInclusive, LocalDateTime toExclusive
    );

    List<Record> findByUnoAndStartTimeBetweenOrderByStartTimeDesc(
            Long uno,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    List<Record> findByUnoAndStartTimeBetween(
            Long uno,
            LocalDateTime from,
            LocalDateTime to
    );
}

package com.paceleague.record.application.port.out;

import com.paceleague.record.application.dto.RecordSummaryProjection;
import com.paceleague.record.domain.entity.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordRepositoryPort {
    Record save(Record record);

    Optional<Record> findBySnoAndUno(Long sno, Long uno);

    Optional<Record> findBySno(Long sno);

    Page<Record> findByUnoOrderByStartTimeDesc(Long uno, Pageable pageable);

    // 관리자 러닝데이터관리 화면 — 전체 회원 대상, 최신순 페이지네이션.
    Page<Record> findAllForAdmin(Pageable pageable);

    List<Record> findByUnoOrderByStartTimeDesc(Long uno);

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

    // 회원 탈퇴 시 러닝 기록 일괄 삭제.
    void deleteAllByUno(Long uno);
}

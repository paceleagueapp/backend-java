package com.example.paceleague.record.repository;

import com.example.paceleague.record.dto.RecordSummaryProjection;
import com.example.paceleague.record.entity.Record;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<Record, Long> {
    // 1개 조회(본인 것만)
    Optional<Record> findBySnoAndUno(Long sno, Long uno);

    // 페이징(본인 것만, 최신순)
    Page<Record> findByUnoOrderByStartTimeDesc(Long uno, Pageable pageable);

    // 한달치 전체 조회(본인 것만)
    List<Record> findByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            Long uno,
            LocalDateTime fromInclusive,
            LocalDateTime toExclusive
    );

    @Query(value = """
            SELECT
                COALESCE(SUM(distance_record), 0) AS totalDistance,
                COALESCE(SUM(TIMESTAMPDIFF(SECOND, start_time, end_time)), 0) AS totalDurationSeconds
            FROM record
            WHERE uno = :uno
            """, nativeQuery = true)
    RecordSummaryProjection findMemberSummary(@Param("uno") Long uno);

    @Query(value = """
            SELECT
                COALESCE(SUM(distance_record), 0) AS totalDistance,
                COALESCE(SUM(TIMESTAMPDIFF(SECOND, start_time, end_time)), 0) AS totalDurationSeconds
            FROM record
            WHERE uno = :uno
              AND start_time >= :fromDt
              AND start_time < :toDt
            """, nativeQuery = true)
    RecordSummaryProjection findMonthSummary(
            @Param("uno") Long uno,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt
    );

    long countByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long uno, LocalDateTime fromInclusive, LocalDateTime toExclusive
    );
}

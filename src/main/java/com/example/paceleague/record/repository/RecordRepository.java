package com.example.paceleague.record.repository;

import com.example.paceleague.record.entity.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.paceleague.record.entity.Record;

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
}

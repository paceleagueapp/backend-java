package com.example.paceleague.record.adapter.out.persistence;

import com.example.paceleague.record.domain.entity.RecordTrack;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordTrackJpaRepository extends JpaRepository<RecordTrack, Long> {
    Optional<RecordTrack> findByUnoAndClientRunId(Long uno, String clientRunId);

    Optional<RecordTrack> findByRecordSno(Long recordSno);

    @Query("""
            select t.sno from RecordTrack t
            where t.status = 'ACTIVE' and t.updateAt < :idleBefore
            order by t.updateAt asc
            """)
    List<Long> findIdleActiveSessionSnos(@Param("idleBefore") LocalDateTime idleBefore, Pageable pageable);
}

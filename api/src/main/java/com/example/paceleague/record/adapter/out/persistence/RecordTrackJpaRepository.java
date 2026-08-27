package com.example.paceleague.record.adapter.out.persistence;

import com.example.paceleague.record.domain.entity.RecordTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordTrackJpaRepository extends JpaRepository<RecordTrack, Long> {
    Optional<RecordTrack> findByUnoAndClientRunId(Long uno, String clientRunId);

    Optional<RecordTrack> findByRecordSno(Long recordSno);
}

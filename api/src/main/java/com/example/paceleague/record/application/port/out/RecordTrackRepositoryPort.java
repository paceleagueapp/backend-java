package com.example.paceleague.record.application.port.out;

import com.example.paceleague.record.domain.entity.RecordTrack;

import java.util.Optional;

public interface RecordTrackRepositoryPort {
    RecordTrack save(RecordTrack track);

    Optional<RecordTrack> findByUnoAndClientRunId(Long uno, String clientRunId);

    Optional<RecordTrack> findByRecordSno(Long recordSno);
}

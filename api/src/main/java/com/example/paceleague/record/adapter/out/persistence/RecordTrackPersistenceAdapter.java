package com.example.paceleague.record.adapter.out.persistence;

import com.example.paceleague.record.application.port.out.RecordTrackRepositoryPort;
import com.example.paceleague.record.domain.entity.RecordTrack;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecordTrackPersistenceAdapter implements RecordTrackRepositoryPort {

    private final RecordTrackJpaRepository recordTrackJpaRepository;

    public RecordTrack save(RecordTrack track) {
        return recordTrackJpaRepository.save(track);
    }

    public Optional<RecordTrack> findByUnoAndClientRunId(Long uno, String clientRunId) {
        return recordTrackJpaRepository.findByUnoAndClientRunId(uno, clientRunId);
    }

    public Optional<RecordTrack> findByRecordSno(Long recordSno) {
        return recordTrackJpaRepository.findByRecordSno(recordSno);
    }
}

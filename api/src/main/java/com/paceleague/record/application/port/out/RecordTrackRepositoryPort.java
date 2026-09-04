package com.paceleague.record.application.port.out;

import com.paceleague.record.domain.entity.RecordTrack;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordTrackRepositoryPort {
    RecordTrack save(RecordTrack track);

    Optional<RecordTrack> findBySno(Long sno);

    Optional<RecordTrack> findByUnoAndClientRunId(Long uno, String clientRunId);

    Optional<RecordTrack> findByRecordSno(Long recordSno);

    // status=ACTIVE 이면서 마지막 갱신(update_at)이 idleBefore 이전인 세션들의 sno를, 오래된 순으로 최대 limit개.
    List<Long> findIdleActiveSessionSnos(LocalDateTime idleBefore, int limit);
}

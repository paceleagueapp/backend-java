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

    // territory 재생(historical replay) 전용 — territory_mode로 끝까지 완주한(FINISHED) 세션 전부의 sno를
    // 실제 러닝이 끝난 시각(ended_at) 오름차순으로. 하나씩 findBySno로 다시 불러와 처리한다
    // (points_json이 러닝당 최대 ~9MB라 전부 한 번에 메모리에 올리지 않기 위함, idle-session 스윕과 동일한 이유).
    List<Long> findFinishedTerritoryModeSnosOrderByEndedAt();
}

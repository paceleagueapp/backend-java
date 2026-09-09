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

    // 이 회원의 트랙 중 record 로 확정된(record_sno IS NOT NULL) 것들의 record_sno 목록.
    // 러닝 목록에서 "이 러닝은 GPS 트랙이 있음" 표시를 채우기 위한 배치 조회(points_json 은 로드하지 않음).
    List<Long> findRecordSnosByUno(Long uno);

    // status=ACTIVE 이면서 마지막 갱신(update_at)이 idleBefore 이전인 세션들의 sno를, 오래된 순으로 최대 limit개.
    List<Long> findIdleActiveSessionSnos(LocalDateTime idleBefore, int limit);

    // territory 재생(historical replay) 전용 — territory_mode로 끝까지 완주한(FINISHED) 세션 전부의 sno를
    // 실제 러닝이 끝난 시각(ended_at) 오름차순으로. 하나씩 findBySno로 다시 불러와 처리한다
    // (points_json이 러닝당 최대 ~9MB라 전부 한 번에 메모리에 올리지 않기 위함, idle-session 스윕과 동일한 이유).
    List<Long> findFinishedTerritoryModeSnosOrderByEndedAt();

    // 회원 탈퇴 시 GPS 트랙 일괄 삭제.
    void deleteAllByUno(Long uno);
}

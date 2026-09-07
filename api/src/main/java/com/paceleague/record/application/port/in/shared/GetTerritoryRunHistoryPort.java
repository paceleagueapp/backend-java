package com.paceleague.record.application.port.in.shared;

import com.paceleague.record.application.dto.TerritoryRunHistoryEntry;

import java.util.List;
import java.util.Optional;

// territory 재생(historical replay, 2026-09-07) 전용 — territory 도메인이 과거 territory_mode 러닝을
// 시간순으로 다시 태우기 위해 record 도메인에서 필요한 것. record→rank의 ApplyScoreUseCase와 반대 방향
// (territory→record) cross-domain 접근이라 shared 패키지에 둔다.
public interface GetTerritoryRunHistoryPort {

    // territory_mode로 끝까지 완주한(FINISHED) 세션 전부의 트랙 sno를 실제 러닝이 끝난 시각 오름차순으로.
    List<Long> findFinishedTerritoryModeTrackSnosOrderByEndedAt();

    // 트랙 하나를 ProcessTerritoryRunCommand로 바로 쓸 수 있는 형태로 조회. 좌표 파싱 실패 등으로
    // 쓸 수 없으면 empty.
    Optional<TerritoryRunHistoryEntry> getEntry(Long trackSno);
}

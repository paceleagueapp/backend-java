package com.example.paceleague.record.application.port.in;

import com.example.paceleague.record.application.dto.GpsSessionRequest;
import com.example.paceleague.record.application.dto.GpsSessionResponse;

import java.time.Duration;
import java.util.List;

public interface SaveGpsSessionUseCase {
    // 앱이 러닝 중 5분마다 보내는 GPS 청크를 받아 clientRunId로 묶어 누적한다.
    // finished=true 청크에서 record 1건을 생성(+점수 산정)하고 세션을 FINISHED로 확정한다.
    GpsSessionResponse ingest(Long uno, GpsSessionRequest req);

    // --- 아래는 스위퍼(GpsSessionSweeper)가 세션마다 별도 트랜잭션으로 호출한다 ---

    // 마지막 갱신이 idleFor 이전인 ACTIVE 세션 sno 목록(오래된 순, 최대 limit개).
    List<Long> findIdleActiveSessionSnos(Duration idleFor, int limit);

    // ACTIVE 세션을 지금까지 쌓인 좌표로 강제 마감(record 생성 + 점수 산정). 이미 처리된 세션이면 아무것도 안 함.
    void finalizeSession(Long trackSno);

    // 마감할 수 없는 ACTIVE 세션을 ABANDONED로 표시.
    void abandonSession(Long trackSno);
}

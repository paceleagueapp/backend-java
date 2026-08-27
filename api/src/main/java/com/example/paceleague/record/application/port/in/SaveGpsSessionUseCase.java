package com.example.paceleague.record.application.port.in;

import com.example.paceleague.record.application.dto.GpsSessionRequest;
import com.example.paceleague.record.application.dto.GpsSessionResponse;

public interface SaveGpsSessionUseCase {
    // 앱이 보낸 GPS 세션을 받아 record 1건을 생성(+점수 산정)하고 GPS 트랙을 저장한다.
    // clientRunId가 이미 저장돼 있으면 새로 만들지 않고 기존 결과를 duplicated=true로 반환.
    GpsSessionResponse save(Long uno, GpsSessionRequest req);
}

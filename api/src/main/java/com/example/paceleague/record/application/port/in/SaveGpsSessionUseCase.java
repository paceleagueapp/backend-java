package com.example.paceleague.record.application.port.in;

import com.example.paceleague.record.application.dto.GpsSessionRequest;
import com.example.paceleague.record.application.dto.GpsSessionResponse;

public interface SaveGpsSessionUseCase {
    // 앱이 러닝 중 5분마다 보내는 GPS 청크를 받아 clientRunId로 묶어 누적한다.
    // finished=true 청크에서 record 1건을 생성(+점수 산정)하고 세션을 FINISHED로 확정한다.
    GpsSessionResponse ingest(Long uno, GpsSessionRequest req);
}

package com.example.paceleague.territory.application.port.in;

import com.example.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.example.paceleague.territory.application.dto.ProcessTerritoryRunResult;

// 땅따먹기 모드로 끝난 러닝 1건을 받아 땅 생성/데미지/회복/점령을 처리한다.
// record 도메인(SaveGpsSessionServiceImpl)이 러닝 종료 시점에 호출한다 —
// record→rank의 ApplyScoreUseCase와 같은 방향의 크로스도메인 유스케이스.
public interface ProcessTerritoryRunUseCase {
    ProcessTerritoryRunResult process(ProcessTerritoryRunCommand command);
}

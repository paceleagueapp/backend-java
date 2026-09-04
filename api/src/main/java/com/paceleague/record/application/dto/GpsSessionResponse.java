package com.paceleague.record.application.dto;

import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;

import java.math.BigDecimal;

// 청크 1건을 처리한 뒤의 러닝 세션 누적 상태.
// status: ACTIVE(진행 중) | FINISHED(종료·record 생성됨)
// acceptedPoints: 이번 요청에서 새로 저장된 좌표 수
// skippedPoints: 이미 저장된 구간과 겹쳐 무시된 좌표 수(청크 재전송 대비)
// recordSno: 종료 전에는 null, finished=true 처리 후 생성된 record.sno
// territoryResult: 땅따먹기 모드(territoryMode=true) 세션이 이번 요청으로 종료됐을 때만 채워짐.
//                 그 외(진행 중 / 일반 러닝 / 이미 종료된 세션에 재전송)에는 null.
public record GpsSessionResponse(
        String clientRunId,
        String status,
        int chunkSeq,
        int acceptedPoints,
        int skippedPoints,
        int totalPoints,
        BigDecimal distanceMeters,
        Long recordSno,
        ProcessTerritoryRunResult territoryResult
) {}

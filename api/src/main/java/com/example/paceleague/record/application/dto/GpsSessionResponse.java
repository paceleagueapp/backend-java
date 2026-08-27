package com.example.paceleague.record.application.dto;

import java.math.BigDecimal;

// 청크 1건을 처리한 뒤의 러닝 세션 누적 상태.
// status: ACTIVE(진행 중) | FINISHED(종료·record 생성됨)
// acceptedPoints: 이번 요청에서 새로 저장된 좌표 수
// skippedPoints: 이미 저장된 구간과 겹쳐 무시된 좌표 수(청크 재전송 대비)
// recordSno: 종료 전에는 null, finished=true 처리 후 생성된 record.sno
public record GpsSessionResponse(
        String clientRunId,
        String status,
        int chunkSeq,
        int acceptedPoints,
        int skippedPoints,
        int totalPoints,
        BigDecimal distanceMeters,
        Long recordSno
) {}

package com.example.paceleague.record.application.dto;

// recordSno: 이 세션으로 생성(또는 멱등 재요청 시 기존)된 record.sno
// trackSno: 저장된 record_track.sno
// duplicated: clientRunId 기준 이미 저장돼 있어 새로 만들지 않고 기존 결과를 돌려준 경우 true
public record GpsSessionResponse(
        Long recordSno,
        Long trackSno,
        boolean duplicated
) {}

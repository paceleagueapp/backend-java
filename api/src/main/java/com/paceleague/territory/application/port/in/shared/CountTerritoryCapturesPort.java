package com.paceleague.territory.application.port.in.shared;

import com.paceleague.territory.application.dto.TerritoryCaptureCount;

import java.time.LocalDateTime;

// 다른 도메인(notification)이 "이 구간에 몇 개의 땅을 몇 명이 점령했나" 를 물어보는 진입 포트.
// from/to 는 territory.create_at 저장 타임존 기준(= 서버 시스템 TZ)의 LocalDateTime.
public interface CountTerritoryCapturesPort {
    TerritoryCaptureCount countBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive);
}

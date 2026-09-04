package com.paceleague.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// @Scheduled 활성화. 현재 유일한 스케줄 작업은 record 도메인의 GpsSessionSweeper.
// 앱 인스턴스가 여러 개가 되면 잡이 중복 실행되므로 ShedLock 등 분산 락 도입이 필요하다(현재는 단일 인스턴스).
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

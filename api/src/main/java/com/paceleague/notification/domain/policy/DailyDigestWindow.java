package com.paceleague.notification.domain.policy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

// "어제(KST)" 하루를 territory.create_at 저장 타임존 기준의 [from, to) LocalDateTime 구간으로 변환한다.
//  - DISPLAY_ZONE(KST) 기준으로 어제 00:00 ~ 오늘 00:00 을 잡고
//  - 그 순간(instant)을 storageZone 으로 다시 표현한다.
// territory.create_at 은 Territory 엔티티가 LocalDateTime.now() 로 채우므로 서버 시스템 TZ(보통 컨테이너=UTC)
// 기준이다. storageZone 에 ZoneId.systemDefault() 를 넘기면 그 가정에 자동으로 맞춰진다.
public final class DailyDigestWindow {

    public static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Seoul");

    private DailyDigestWindow() {}

    public record Window(LocalDateTime fromInclusive, LocalDateTime toExclusive) {}

    public static Window forDate(LocalDate targetDateKst, ZoneId storageZone) {
        LocalDateTime from = targetDateKst.atStartOfDay(DISPLAY_ZONE)
                .withZoneSameInstant(storageZone).toLocalDateTime();
        LocalDateTime to = targetDateKst.plusDays(1).atStartOfDay(DISPLAY_ZONE)
                .withZoneSameInstant(storageZone).toLocalDateTime();
        return new Window(from, to);
    }

    public static LocalDate yesterdayKst() {
        return LocalDate.now(DISPLAY_ZONE).minusDays(1);
    }
}

package com.paceleague.territory.adapter.in.batch;

import com.paceleague.territory.application.dto.TerritoryReplaySummary;
import com.paceleague.territory.application.service.TerritoryHistoricalReplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// 2026-09-07: 겹침 판정이 territory 전체 단위 → 헥사곤 단위로 바뀌면서, 그 이전 로직으로 이미 저장된
// 운영 데이터(통째로 뺏긴 땅들)를 새 규칙으로 다시 계산하는 1회성 배치. TerritoryHexBackfillRunner와
// 같은 @ConditionalOnProperty 패턴이지만 **이건 멱등하지 않다** — 기존 territory 데이터를 전부 지우고
// 다시 만든다. paceleague.territory.replay.enabled=true 로 켠 상태로 앱을 띄운 순간에만 실행되며,
// 성공을 확인한 뒤 반드시 플래그를 다시 꺼야 한다(그대로 켜둔 채 재시작하면 매번 다시 전체 재생 —
// 그 사이 실제 유저가 만든 땅까지 포함해서 재생되므로 안전하긴 하지만, 재생 도중 들어오는 새 러닝과는
// 경합할 수 있어 트래픽이 적은 시간에 한 번만 돌리는 것을 권장).
@Component
@ConditionalOnProperty(name = "paceleague.territory.replay.enabled", havingValue = "true")
public class TerritoryHistoricalReplayRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TerritoryHistoricalReplayRunner.class);

    private final TerritoryHistoricalReplayService territoryHistoricalReplayService;

    public TerritoryHistoricalReplayRunner(TerritoryHistoricalReplayService territoryHistoricalReplayService) {
        this.territoryHistoricalReplayService = territoryHistoricalReplayService;
    }

    @Override
    public void run(ApplicationArguments args) {
        TerritoryReplaySummary summary = territoryHistoricalReplayService.replay();
        log.warn("territory historical replay runner 종료 — 결과 확인 후 " +
                "paceleague.territory.replay.enabled 플래그를 반드시 다시 꺼주세요. summary={}", summary);
    }
}

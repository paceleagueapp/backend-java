package com.paceleague.territory.adapter.in.batch;

import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import com.paceleague.territory.application.service.TerritoryHexBackfillService;
import com.paceleague.territory.domain.entity.Territory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

// H3 도입(2026-09-05) 이전에 생성된 ACTIVE 땅(territory_hex 행이 없는 "유령 땅")을 한 번 훑어
// 헥사곤으로 환산하는 1회성 배치. 기본적으로 꺼져 있고(matchIfMissing 없음 = 프로퍼티가 없으면 빈 자체가
// 안 만들어짐), paceleague.territory.backfill.enabled=true 로 켠 상태로 앱을 띄운 순간에만 실행된다.
// TerritoryHexBackfillService.backfillOne이 "이미 territory_hex가 있는 땅은 손대지 않는" 게 아니라
// findActiveMissingHex 자체가 그 땅들을 애초에 안 돌려주므로(TerritoryJpaRepository 참고) 멱등하다 —
// 켜둔 채로 재시작해도 매번 새로 백필할 대상이 없으면 그냥 아무 일도 안 하고 끝난다.
@Component
@ConditionalOnProperty(name = "paceleague.territory.backfill.enabled", havingValue = "true")
public class TerritoryHexBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TerritoryHexBackfillRunner.class);

    private final TerritoryRepositoryPort territoryRepositoryPort;
    private final TerritoryHexBackfillService territoryHexBackfillService;

    public TerritoryHexBackfillRunner(TerritoryRepositoryPort territoryRepositoryPort,
                                      TerritoryHexBackfillService territoryHexBackfillService) {
        this.territoryRepositoryPort = territoryRepositoryPort;
        this.territoryHexBackfillService = territoryHexBackfillService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Territory> targets = territoryRepositoryPort.findActiveMissingHex();
        if (targets.isEmpty()) {
            log.info("territory hex backfill: territory_hex 누락된 ACTIVE 땅 없음, 종료");
            return;
        }
        log.info("territory hex backfill: 대상 {}건 시작", targets.size());

        int migrated = 0;
        int unclaimable = 0;
        for (Territory t : targets) {
            try {
                if (territoryHexBackfillService.backfillOne(t.getSno())) {
                    migrated++;
                } else {
                    unclaimable++;
                }
            } catch (Exception e) {
                unclaimable++;
                log.error("territory hex backfill: territory sno={} 실패", t.getSno(), e);
            }
        }
        log.info("territory hex backfill 완료: 총 {}건 중 {}건 백필, {}건 실패/유령땅으로 남음",
                targets.size(), migrated, unclaimable);
    }
}

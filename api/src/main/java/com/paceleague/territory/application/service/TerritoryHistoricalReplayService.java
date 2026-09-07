package com.paceleague.territory.application.service;

import com.paceleague.record.application.dto.TerritoryRunHistoryEntry;
import com.paceleague.record.application.port.in.shared.GetTerritoryRunHistoryPort;
import com.paceleague.territory.application.dto.ProcessTerritoryRunCommand;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult.Outcome;
import com.paceleague.territory.application.dto.TerritoryReplaySummary;
import com.paceleague.territory.application.port.in.shared.ProcessTerritoryRunUseCase;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

// 2026-09-07: 겹침 판정을 territory 전체 → 헥사곤 단위로 바꾸면서, 그 전에 이미 "통째로 뺏긴" 채로
// 저장된 운영 데이터를 새 규칙 기준으로 다시 계산하기 위한 1회성 배치. TerritoryHistoricalReplayRunner가
// paceleague.territory.replay.enabled=true 로 켠 상태로 앱을 띄운 순간에만 이 서비스를 호출한다.
//
// **TerritoryHexBackfillRunner와 달리 멱등하지 않다 — 매번 기존 territory/territory_hex를 전부 지우고
// record_track 이력을 처음부터 다시 재생한다.** 켠 채로 재배포/재시작을 반복하면 그 사이 실제 유저가
// 새로 만든 땅까지는 안전하다(재생 대상도 매번 최신 FINISHED 이력 전체이므로 결과에 포함됨)만,
// 재생 도중 들어오는 새 러닝과는 경합할 수 있다 — 트래픽이 적은 시간에, 한 번 성공 확인 후에는
// 반드시 플래그를 다시 꺼야 한다(트루-원샷 스위치; 백필 러너의 "켜둬도 안전" 전제가 여기엔 없다).
@Service
@RequiredArgsConstructor
public class TerritoryHistoricalReplayService {

    private static final Logger log = LoggerFactory.getLogger(TerritoryHistoricalReplayService.class);

    private final TerritoryRepositoryPort territoryRepositoryPort;
    private final TerritoryHexRepositoryPort territoryHexRepositoryPort;
    private final GetTerritoryRunHistoryPort getTerritoryRunHistoryPort;
    private final ProcessTerritoryRunUseCase processTerritoryRunUseCase;

    public TerritoryReplaySummary replay() {
        log.warn("!!! territory historical replay: 기존 territory/territory_hex 전체 삭제 후 " +
                "record_track 이력을 새 규칙으로 재생성합니다 !!!");
        territoryHexRepositoryPort.deleteAll();
        territoryRepositoryPort.deleteAll();

        List<Long> trackSnos = getTerritoryRunHistoryPort.findFinishedTerritoryModeTrackSnosOrderByEndedAt();
        log.warn("territory historical replay: 대상 {}건(시간순) 재생 시작", trackSnos.size());

        int created = 0;
        int interacted = 0;
        int skipped = 0;
        int failed = 0;
        for (Long trackSno : trackSnos) {
            try {
                TerritoryRunHistoryEntry entry = getTerritoryRunHistoryPort.getEntry(trackSno).orElse(null);
                if (entry == null) {
                    skipped++;
                    continue;
                }
                ProcessTerritoryRunResult result = processTerritoryRunUseCase.process(new ProcessTerritoryRunCommand(
                        entry.memberSno(), entry.recordSno(), entry.trackSno(), entry.coords(),
                        entry.startedAt(), entry.endedAt()));
                if (result.outcome() == Outcome.CREATED) {
                    created++;
                } else if (result.outcome() == Outcome.INTERACTED) {
                    interacted++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                failed++;
                log.error("territory historical replay: trackSno={} 재생 실패", trackSno, e);
            }
        }

        TerritoryReplaySummary summary = new TerritoryReplaySummary(trackSnos.size(), created, interacted, skipped, failed);
        log.warn("territory historical replay 완료: 총 {}건 중 생성 {}건, 상호작용 {}건, 스킵 {}건, 실패 {}건",
                summary.totalRuns(), summary.created(), summary.interacted(), summary.skipped(), summary.failed());
        return summary;
    }
}

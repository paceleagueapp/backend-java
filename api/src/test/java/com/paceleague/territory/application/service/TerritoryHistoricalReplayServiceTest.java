package com.paceleague.territory.application.service;

import com.paceleague.record.application.dto.TerritoryRunHistoryEntry;
import com.paceleague.record.application.port.in.shared.GetTerritoryRunHistoryPort;
import com.paceleague.territory.application.dto.ProcessTerritoryRunResult;
import com.paceleague.territory.application.dto.TerritoryReplaySummary;
import com.paceleague.territory.application.port.in.shared.ProcessTerritoryRunUseCase;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerritoryHistoricalReplayServiceTest {

    @Mock
    TerritoryRepositoryPort territoryRepositoryPort;
    @Mock
    TerritoryHexRepositoryPort territoryHexRepositoryPort;
    @Mock
    GetTerritoryRunHistoryPort getTerritoryRunHistoryPort;
    @Mock
    ProcessTerritoryRunUseCase processTerritoryRunUseCase;

    private TerritoryHistoricalReplayService newService() {
        return new TerritoryHistoricalReplayService(
                territoryRepositoryPort, territoryHexRepositoryPort, getTerritoryRunHistoryPort, processTerritoryRunUseCase);
    }

    private TerritoryRunHistoryEntry entry(Long trackSno) {
        return new TerritoryRunHistoryEntry(100L, 900L + trackSno, trackSno, List.of(new double[]{37.5, 127.0}),
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now());
    }

    @Test
    void 재생_전_기존_territory_데이터를_전부_지우고_시간순으로_재생한다() {
        when(getTerritoryRunHistoryPort.findFinishedTerritoryModeTrackSnosOrderByEndedAt())
                .thenReturn(List.of(1L, 2L, 3L));
        when(getTerritoryRunHistoryPort.getEntry(1L)).thenReturn(Optional.of(entry(1L)));
        when(getTerritoryRunHistoryPort.getEntry(2L)).thenReturn(Optional.of(entry(2L)));
        when(getTerritoryRunHistoryPort.getEntry(3L)).thenReturn(Optional.of(entry(3L)));
        when(processTerritoryRunUseCase.process(any()))
                .thenReturn(ProcessTerritoryRunResult.created(10L))
                .thenReturn(ProcessTerritoryRunResult.interacted(11L, List.of(), List.of(), List.of()))
                .thenReturn(ProcessTerritoryRunResult.noLoop());

        TerritoryReplaySummary summary = newService().replay();

        InOrder order = inOrder(territoryHexRepositoryPort, territoryRepositoryPort, processTerritoryRunUseCase);
        order.verify(territoryHexRepositoryPort).deleteAll();
        order.verify(territoryRepositoryPort).deleteAll();
        order.verify(processTerritoryRunUseCase).process(argThatTrackSno(1L));
        order.verify(processTerritoryRunUseCase).process(argThatTrackSno(2L));
        order.verify(processTerritoryRunUseCase).process(argThatTrackSno(3L));

        assertThat(summary.totalRuns()).isEqualTo(3);
        assertThat(summary.created()).isEqualTo(1);
        assertThat(summary.interacted()).isEqualTo(1);
        assertThat(summary.skipped()).isEqualTo(1); // NO_LOOP
        assertThat(summary.failed()).isEqualTo(0);
    }

    @Test
    void 좌표_파싱에_실패한_트랙은_건너뛰고_process를_호출하지_않는다() {
        when(getTerritoryRunHistoryPort.findFinishedTerritoryModeTrackSnosOrderByEndedAt())
                .thenReturn(List.of(1L));
        when(getTerritoryRunHistoryPort.getEntry(1L)).thenReturn(Optional.empty());

        TerritoryReplaySummary summary = newService().replay();

        assertThat(summary.skipped()).isEqualTo(1);
        assertThat(summary.failed()).isEqualTo(0);
        org.mockito.Mockito.verify(processTerritoryRunUseCase, org.mockito.Mockito.never()).process(any());
    }

    @Test
    void 한_건이_처리_중_예외를_던져도_나머지는_계속_재생한다() {
        when(getTerritoryRunHistoryPort.findFinishedTerritoryModeTrackSnosOrderByEndedAt())
                .thenReturn(List.of(1L, 2L));
        when(getTerritoryRunHistoryPort.getEntry(1L)).thenReturn(Optional.of(entry(1L)));
        when(getTerritoryRunHistoryPort.getEntry(2L)).thenReturn(Optional.of(entry(2L)));
        when(processTerritoryRunUseCase.process(any()))
                .thenThrow(new IllegalStateException("boom"))
                .thenReturn(ProcessTerritoryRunResult.created(10L));

        TerritoryReplaySummary summary = newService().replay();

        assertThat(summary.failed()).isEqualTo(1);
        assertThat(summary.created()).isEqualTo(1);
    }

    private static com.paceleague.territory.application.dto.ProcessTerritoryRunCommand argThatTrackSno(Long trackSno) {
        return org.mockito.ArgumentMatchers.argThat(cmd -> cmd != null && trackSno.equals(cmd.trackSno()));
    }
}

package com.example.paceleague.record.adapter.in.scheduler;

import com.example.paceleague.record.application.port.in.SaveGpsSessionUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 스위퍼가 일부 sno만 예외 stub하고 나머지는 기본 동작으로 호출하므로
class GpsSessionSweeperTest {

    @Mock
    SaveGpsSessionUseCase useCase;

    private GpsSessionSweeper sweeper() {
        return new GpsSessionSweeper(useCase, 30);
    }

    @Test
    void 대상이_없으면_아무것도_하지_않는다() {
        when(useCase.findIdleActiveSessionSnos(any(), anyInt())).thenReturn(List.of());

        sweeper().sweep();

        verify(useCase, never()).finalizeSession(any());
        verify(useCase, never()).abandonSession(any());
    }

    @Test
    void 유휴_세션마다_finalizeSession을_호출한다() {
        when(useCase.findIdleActiveSessionSnos(any(), anyInt())).thenReturn(List.of(1L, 2L, 3L));

        sweeper().sweep();

        verify(useCase).finalizeSession(1L);
        verify(useCase).finalizeSession(2L);
        verify(useCase).finalizeSession(3L);
        verify(useCase, never()).abandonSession(any());
    }

    @Test
    void finalize가_실패한_세션은_abandon으로_넘긴다() {
        when(useCase.findIdleActiveSessionSnos(any(), anyInt())).thenReturn(List.of(1L, 2L));
        doThrow(new IllegalArgumentException("distanceRecord must be positive"))
                .when(useCase).finalizeSession(2L);

        sweeper().sweep();

        verify(useCase).finalizeSession(1L);
        verify(useCase).finalizeSession(2L);
        verify(useCase).abandonSession(2L);
        verify(useCase, never()).abandonSession(1L);
    }

    @Test
    void 한_세션의_실패가_다음_세션_처리를_막지_않는다() {
        when(useCase.findIdleActiveSessionSnos(any(), anyInt())).thenReturn(List.of(1L, 2L, 3L));
        doThrow(new RuntimeException("boom")).when(useCase).finalizeSession(1L);

        sweeper().sweep();

        verify(useCase).finalizeSession(2L);
        verify(useCase).finalizeSession(3L);
        verify(useCase).abandonSession(1L);
    }

    @Test
    void idle_임계값이_Duration으로_전달된다() {
        when(useCase.findIdleActiveSessionSnos(any(), anyInt())).thenReturn(List.of());

        sweeper().sweep();

        verify(useCase).findIdleActiveSessionSnos(eq(Duration.ofMinutes(30)), anyInt());
    }
}

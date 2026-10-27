package com.paceleague.notification.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberJoinedAtPort;
import com.paceleague.notification.application.dto.NotificationHistoryPage;
import com.paceleague.notification.application.port.out.NotificationHistoryRepositoryPort;
import com.paceleague.notification.domain.entity.NotificationHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationHistoryQueryServiceTest {

    @Mock NotificationHistoryRepositoryPort repositoryPort;
    @Mock GetMemberJoinedAtPort getMemberJoinedAtPort;

    NotificationHistoryQueryService service;

    private final Long memberSno = 7L;
    private final Instant joinedAt = Instant.parse("2026-10-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        service = new NotificationHistoryQueryService(repositoryPort, getMemberJoinedAtPort);
    }

    // sno 가 내림차순인 알림 n 건
    private List<NotificationHistory> rows(long fromSno, int n) {
        List<NotificationHistory> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            NotificationHistory h = NotificationHistory.broadcast("K", "t" + i, "b" + i, null);
            ReflectionTestUtils.setField(h, "sno", fromSno - i);
            list.add(h);
        }
        return list;
    }

    @Test
    void 다음_페이지가_있으면_size개만_내리고_마지막_sno를_nextCursor로_준다() {
        when(getMemberJoinedAtPort.getJoinedAt(memberSno)).thenReturn(Optional.of(joinedAt));
        when(repositoryPort.findVisibleTo(eq(memberSno), any(), isNull(), eq(6))).thenReturn(rows(100, 6));

        NotificationHistoryPage page = service.getHistory(memberSno, null, 5);

        assertThat(page.items()).hasSize(5);
        assertThat(page.hasMore()).isTrue();
        assertThat(page.nextCursor()).isEqualTo(96L);
    }

    @Test
    void 마지막_페이지면_hasMore_false_nextCursor_null() {
        when(getMemberJoinedAtPort.getJoinedAt(memberSno)).thenReturn(Optional.of(joinedAt));
        when(repositoryPort.findVisibleTo(eq(memberSno), any(), eq(96L), eq(6))).thenReturn(rows(95, 3));

        NotificationHistoryPage page = service.getHistory(memberSno, 96L, 5);

        assertThat(page.items()).hasSize(3);
        assertThat(page.hasMore()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    void 전체발송분은_가입시각_이후만_보도록_가입시각을_조회조건으로_넘긴다() {
        when(getMemberJoinedAtPort.getJoinedAt(memberSno)).thenReturn(Optional.of(joinedAt));
        when(repositoryPort.findVisibleTo(any(), any(), any(), anyInt())).thenReturn(List.of());

        service.getHistory(memberSno, null, 5);

        verify(repositoryPort).findVisibleTo(memberSno,
                LocalDateTime.ofInstant(joinedAt, ZoneId.systemDefault()), null, 6);
    }

    @Test
    void size는_1에서_50_사이로_보정된다() {
        when(getMemberJoinedAtPort.getJoinedAt(memberSno)).thenReturn(Optional.of(joinedAt));
        when(repositoryPort.findVisibleTo(any(), any(), any(), anyInt())).thenReturn(List.of());

        service.getHistory(memberSno, null, 1000);
        verify(repositoryPort).findVisibleTo(any(), any(), any(), eq(51));

        service.getHistory(memberSno, null, 0);
        verify(repositoryPort).findVisibleTo(any(), any(), any(), eq(2));
    }

    @Test
    void 회원을_찾을_수_없으면_빈_목록을_준다() {
        when(getMemberJoinedAtPort.getJoinedAt(memberSno)).thenReturn(Optional.empty());

        NotificationHistoryPage page = service.getHistory(memberSno, null, 5);

        assertThat(page.items()).isEmpty();
        assertThat(page.hasMore()).isFalse();
        verify(repositoryPort, never()).findVisibleTo(any(), any(), any(), anyInt());
    }
}

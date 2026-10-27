package com.paceleague.popup.application.service;

import com.paceleague.popup.application.dto.AdminPopupResponse;
import com.paceleague.popup.application.dto.PopupCommand;
import com.paceleague.popup.application.dto.PopupResponse;
import com.paceleague.popup.application.port.out.PopupRepositoryPort;
import com.paceleague.popup.domain.entity.Popup;
import com.paceleague.popup.domain.enums.PopupTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PopupServiceTest {

    @Mock PopupRepositoryPort popupRepositoryPort;
    @InjectMocks PopupService service;

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 1, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 11, 8, 0, 0);

    private PopupCommand command(String title, String content, PopupTarget target,
                                 LocalDateTime start, LocalDateTime end, Boolean active) {
        return new PopupCommand(title, content, target, start, end, active);
    }

    @Test
    void 등록하면_본문을_정제하고_active_생략시_활성으로_저장한다() {
        when(popupRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdminPopupResponse res = service.create(command(" 공지 ", "<p>안녕<script>alert(1)</script></p>",
                PopupTarget.BOTH, START, END, null));

        assertThat(res.title()).isEqualTo("공지");
        assertThat(res.content()).doesNotContain("script").contains("안녕");
        assertThat(res.active()).isTrue();
        assertThat(res.target()).isEqualTo(PopupTarget.BOTH);
    }

    @Test
    void 종료가_시작보다_빠르거나_같으면_400() {
        assertThatThrownBy(() -> service.create(command("t", "<p>x</p>", PopupTarget.WEB, END, START, true)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(command("t", "<p>x</p>", PopupTarget.WEB, START, START, true)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(popupRepositoryPort, never()).save(any());
    }

    @Test
    void 제목_대상_기간_본문이_없으면_400() {
        assertThatThrownBy(() -> service.create(command(" ", "<p>x</p>", PopupTarget.WEB, START, END, true)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(command("t", "<p>x</p>", null, START, END, true)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(command("t", "<p>x</p>", PopupTarget.APP, null, END, true)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(command("t", "<p> </p><script>x</script>", PopupTarget.APP, START, END, true)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미지만_있는_본문도_허용한다() {
        when(popupRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdminPopupResponse res = service.create(command("t", "<img src=\"https://a.example/x.png\">",
                PopupTarget.APP, START, END, true));

        assertThat(res.content()).contains("<img");
    }

    @Test
    void 공개조회는_KST_현재시각으로_노출중인_팝업만_조회한다() {
        Popup popup = Popup.create("t", "<p>x</p>", PopupTarget.WEB, START, END, true);
        when(popupRepositoryPort.findVisibleAt(any())).thenReturn(List.of(popup));

        List<PopupResponse> result = service.getActivePopups();

        ArgumentCaptor<LocalDateTime> now = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(popupRepositoryPort).findVisibleAt(now.capture());
        assertThat(now.getValue()).isCloseTo(LocalDateTime.now(PopupService.KST),
                org.assertj.core.api.Assertions.within(5, java.time.temporal.ChronoUnit.SECONDS));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).target()).isEqualTo(PopupTarget.WEB);
    }

    @Test
    void 비활성화하면_기간_안이어도_노출되지_않는다() {
        LocalDateTime now = LocalDateTime.now(PopupService.KST);
        Popup popup = Popup.create("t", "<p>x</p>", PopupTarget.WEB, now.minusDays(1), now.plusDays(1), true);
        when(popupRepositoryPort.findById(1L)).thenReturn(Optional.of(popup));
        when(popupRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.get(1L).visibleNow()).isTrue();
        AdminPopupResponse res = service.changeActive(1L, false);

        assertThat(res.active()).isFalse();
        assertThat(res.visibleNow()).isFalse();
    }

    @Test
    void 종료시각은_포함하지_않는다() {
        Popup popup = Popup.create("t", "<p>x</p>", PopupTarget.WEB, START, END, true);

        assertThat(popup.isVisibleAt(START)).isTrue();
        assertThat(popup.isVisibleAt(END.minusSeconds(1))).isTrue();
        assertThat(popup.isVisibleAt(END)).isFalse();
        assertThat(popup.isVisibleAt(START.minusSeconds(1))).isFalse();
    }

    @Test
    void 없는_팝업은_400() {
        when(popupRepositoryPort.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L, command("t", "<p>x</p>", PopupTarget.WEB, START, END, true)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.delete(9L)).isInstanceOf(IllegalArgumentException.class);
    }
}

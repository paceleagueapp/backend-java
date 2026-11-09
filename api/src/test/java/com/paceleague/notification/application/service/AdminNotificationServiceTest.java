package com.paceleague.notification.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.notification.application.dto.AdminNotificationDetail;
import com.paceleague.notification.application.port.in.shared.SendMemberPushPort;
import com.paceleague.notification.application.port.out.AdminNotificationRepositoryPort;
import com.paceleague.notification.domain.entity.AdminNotification;
import com.paceleague.notification.domain.entity.AdminNotificationTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminNotificationServiceTest {

    @Mock AdminNotificationRepositoryPort repositoryPort;
    @Mock SendMemberPushPort sendMemberPushPort;
    @Mock GetMemberNicknamePort getMemberNicknamePort;

    AdminNotificationService service;

    @BeforeEach
    void setUp() {
        service = new AdminNotificationService(repositoryPort, sendMemberPushPort, getMemberNicknamePort);
        when(repositoryPort.save(any())).thenAnswer(inv -> {
            AdminNotification n = inv.getArgument(0);
            if (n.getSno() == null) ReflectionTestUtils.setField(n, "sno", 10L);
            return n;
        });
        when(getMemberNicknamePort.getNicknames(any())).thenAnswer(inv -> {
            Map<Long, String> names = new HashMap<>();
            for (Long sno : (Collection<Long>) inv.getArgument(0)) names.put(sno, "회원" + sno);
            return names;
        });
    }

    @Test
    void 고른_회원마다_푸시하고_발송수와_대상을_저장한다() {
        when(sendMemberPushPort.send(eq(1L), anyString(), anyString(), anyString(), anyString(), anyMap())).thenReturn(true);
        when(sendMemberPushPort.send(eq(2L), anyString(), anyString(), anyString(), anyString(), anyMap())).thenReturn(false);

        AdminNotificationDetail result = service.send(99L, " 공지 ", "<p>안녕하세요</p>", List.of(1L, 2L, 1L));

        assertThat(result.sno()).isEqualTo(10L);
        assertThat(result.targetCount()).isEqualTo(2);
        assertThat(result.sentCount()).isEqualTo(1);
        assertThat(result.title()).isEqualTo("공지");
        assertThat(result.targets()).extracting(AdminNotificationDetail.Target::nickname).containsExactly("회원1", "회원2");
        verify(sendMemberPushPort).send(eq(1L), eq("ADMIN_NOTICE"), eq("공지"), eq("안녕하세요"),
                eq("paceleague://notifications/10"), anyMap());

        ArgumentCaptor<List<AdminNotificationTarget>> captor = ArgumentCaptor.forClass(List.class);
        verify(repositoryPort).saveTargets(captor.capture());
        assertThat(captor.getValue()).extracting(AdminNotificationTarget::isSent).containsExactly(true, false);
    }

    @Test
    void 이미지만_있는_알림은_고정_문구로_푸시한다() {
        service.send(99L, "이벤트", "<p><img src=\"https://paceleague-media.s3.amazonaws.com/media/a.png\"></p>", List.of(1L));

        verify(sendMemberPushPort).send(eq(1L), anyString(), anyString(), eq(AdminNotificationService.IMAGE_ONLY_BODY),
                anyString(), anyMap());
    }

    @Test
    void 긴_본문은_푸시에서_잘린다() {
        String body = AdminNotificationService.pushBody("<p>" + "가".repeat(300) + "</p>");
        assertThat(body).hasSize(AdminNotificationService.PUSH_BODY_MAX_LENGTH + 1).endsWith("…");
    }

    @Test
    void 대상이_없으면_400() {
        assertThatThrownBy(() -> service.send(99L, "t", "<p>b</p>", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repositoryPort, never()).save(any());
    }

    @Test
    void 대상이_1000명을_넘으면_400() {
        List<Long> many = new ArrayList<>(LongStream.rangeClosed(1, 1001).boxed().toList());
        assertThatThrownBy(() -> service.send(99L, "t", "<p>b</p>", many))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 제목_또는_내용이_비면_400() {
        assertThatThrownBy(() -> service.send(99L, " ", "<p>b</p>", List.of(1L))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.send(99L, "t", "<p> </p>", List.of(1L))).isInstanceOf(IllegalArgumentException.class);
        verify(sendMemberPushPort, never()).send(anyLong(), anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void 받는_회원만_앱에서_상세를_볼_수_있다() {
        AdminNotification n = AdminNotification.create("t", "<p>b</p>", 1, 99L);
        ReflectionTestUtils.setField(n, "sno", 10L);
        when(repositoryPort.findById(10L)).thenReturn(Optional.of(n));
        when(repositoryPort.isTarget(10L, 1L)).thenReturn(true);
        when(repositoryPort.isTarget(10L, 2L)).thenReturn(false);

        assertThat(service.getNotice(1L, 10L).content()).isEqualTo("<p>b</p>");
        assertThatThrownBy(() -> service.getNotice(2L, 10L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 푸시_데이터에_알림번호와_딥링크를_싣는다() {
        service.send(99L, "t", "<p>b</p>", List.of(1L));

        ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
        verify(sendMemberPushPort, times(1)).send(eq(1L), anyString(), anyString(), anyString(), anyString(), data.capture());
        assertThat(data.getValue()).containsEntry("type", "notice").containsEntry("notificationSno", "10")
                .containsEntry("deeplink", "paceleague://notifications/10");
    }
}

package com.paceleague.member.application.service;

import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.member.application.port.out.MemberBlockRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberBlockServiceTest {

    @Mock MemberBlockRepositoryPort repo;
    @Mock GetMemberNicknamePort getMemberNicknamePort;

    MemberBlockService service() {
        return new MemberBlockService(repo, getMemberNicknamePort);
    }

    @Test
    void 자기_자신은_차단할_수_없다() {
        assertThatThrownBy(() -> service().block(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void 이미_차단했으면_다시_저장하지_않는다() {
        when(repo.exists(1L, 2L)).thenReturn(true);
        service().block(1L, 2L);
        verify(repo, never()).save(any());
    }

    @Test
    void 처음_차단이면_저장한다() {
        when(repo.exists(1L, 2L)).thenReturn(false);
        service().block(1L, 2L);
        verify(repo).save(any());
    }

    @Test
    void 비로그인_조회자의_차단목록은_빈_집합() {
        assertThat(service().getBlockedBy(null)).isEmpty();
        verify(repo, never()).findBlockedSnos(anyLong());
    }

    @Test
    void 로그인_조회자는_차단_sno_집합을_돌려준다() {
        when(repo.findBlockedSnos(1L)).thenReturn(Set.of(2L, 3L));
        assertThat(service().getBlockedBy(1L)).containsExactlyInAnyOrder(2L, 3L);
    }
}

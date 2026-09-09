package com.paceleague.member.application.service;

import com.paceleague.board.application.port.in.shared.PurgeMemberBoardPort;
import com.paceleague.crew.application.port.in.shared.LeaveCrewOnWithdrawPort;
import com.paceleague.media.application.port.in.shared.PurgeMemberMediaPort;
import com.paceleague.member.application.port.out.MemberBlockRepositoryPort;
import com.paceleague.member.application.port.out.MemberRepositoryPort;
import com.paceleague.member.domain.entity.Member;
import com.paceleague.rank.application.port.in.shared.PurgeMemberRankPort;
import com.paceleague.record.application.port.in.shared.PurgeMemberRecordsPort;
import com.paceleague.territory.application.port.in.shared.PurgeMemberTerritoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberWithdrawServiceTest {

    @Mock MemberRepositoryPort memberRepositoryPort;
    @Mock MemberBlockRepositoryPort memberBlockRepositoryPort;
    @Mock PasswordEncoder passwordEncoder;
    @Mock LeaveCrewOnWithdrawPort leaveCrewOnWithdrawPort;
    @Mock PurgeMemberRecordsPort purgeMemberRecordsPort;
    @Mock PurgeMemberRankPort purgeMemberRankPort;
    @Mock PurgeMemberTerritoryPort purgeMemberTerritoryPort;
    @Mock PurgeMemberMediaPort purgeMemberMediaPort;
    @Mock PurgeMemberBoardPort purgeMemberBoardPort;

    MemberWithdrawService service;
    Member member;

    @BeforeEach
    void setUp() {
        service = new MemberWithdrawService(memberRepositoryPort, memberBlockRepositoryPort, passwordEncoder,
                leaveCrewOnWithdrawPort, purgeMemberRecordsPort, purgeMemberRankPort, purgeMemberTerritoryPort,
                purgeMemberMediaPort, purgeMemberBoardPort);
        member = Member.create("runner1", "HASH", "달리는곰", "a@b.com");
        ReflectionTestUtils.setField(member, "sno", 42);
        when(memberRepositoryPort.findBySno(42L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("pw", "HASH")).thenReturn(true);
    }

    @Test
    void 정상_탈퇴시_데이터를_전부_purge하고_member를_마스킹한다() {
        service.withdraw(42L, "pw");

        verify(leaveCrewOnWithdrawPort).onMemberWithdraw(42L);
        verify(purgeMemberRecordsPort).purge(42L);
        verify(purgeMemberRankPort).purge(42L);
        verify(purgeMemberTerritoryPort).purge(42L);
        verify(purgeMemberMediaPort).purge(42L);
        verify(purgeMemberBoardPort).purge(42L);
        verify(memberRepositoryPort).save(member);

        assertThat(member.isActive()).isFalse();
        assertThat(member.getStatus()).isEqualTo(Member.STATUS_WITHDRAWN);
        assertThat(member.getMemberId()).isEqualTo("withdrawn_42");
        assertThat(member.getNickname()).isNull();
        assertThat(member.getEmail()).isNull();
        assertThat(member.getPasswordHash()).isEmpty();
        assertThat(member.getWithdrawnAt()).isNotNull();
    }

    @Test
    void 비밀번호가_틀리면_400이고_purge하지_않는다() {
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> service.withdraw(42L, "wrong"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(purgeMemberRecordsPort, never()).purge(anyLong());
        verify(memberRepositoryPort, never()).save(any());
        assertThat(member.isActive()).isTrue();
    }

    @Test
    void 이미_탈퇴한_회원이면_멱등하게_아무것도_안_한다() {
        member.withdraw();

        service.withdraw(42L, "pw");

        verify(leaveCrewOnWithdrawPort, never()).onMemberWithdraw(anyLong());
        verify(purgeMemberRecordsPort, never()).purge(anyLong());
        verify(memberRepositoryPort, never()).save(any());
    }

    @Test
    void 크루장이면_예외가_전파되고_member는_그대로다() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("크루장은 먼저 위임/해체"))
                .when(leaveCrewOnWithdrawPort).onMemberWithdraw(42L);

        assertThatThrownBy(() -> service.withdraw(42L, "pw"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(purgeMemberRecordsPort, never()).purge(anyLong());
        assertThat(member.isActive()).isTrue();
    }
}

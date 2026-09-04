package com.paceleague.crew.application.service;

import com.paceleague.crew.application.port.out.CrewInvitationRepositoryPort;
import com.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.paceleague.crew.config.CrewProperties;
import com.paceleague.crew.domain.entity.Crew;
import com.paceleague.crew.domain.entity.CrewInvitation;
import com.paceleague.crew.domain.entity.CrewMember;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CrewInvitationServiceTest {

    @Mock CrewRepositoryPort crewRepositoryPort;
    @Mock CrewMemberRepositoryPort crewMemberRepositoryPort;
    @Mock CrewInvitationRepositoryPort crewInvitationRepositoryPort;
    @Mock GetMemberNicknamePort getMemberNicknamePort;

    CrewInvitationService service;

    @BeforeEach
    void setUp() {
        CrewProperties props = new CrewProperties(null, null, null, null, null, null, null);
        CrewMembershipManager manager = new CrewMembershipManager(crewRepositoryPort, crewMemberRepositoryPort);
        service = new CrewInvitationService(crewRepositoryPort, crewMemberRepositoryPort,
                crewInvitationRepositoryPort, manager, getMemberNicknamePort, props);
        when(crewRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(crewMemberRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(crewInvitationRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 초대_수락_시_크루원으로_가입되고_초대는_ACCEPTED가_된다() {
        Crew crew = Crew.create("달리는곰", null, null, 30, 100L);
        CrewInvitation inv = CrewInvitation.create(1L, 100L, 200L, LocalDateTime.now().plusDays(3));
        when(crewInvitationRepositoryPort.findBySno(9L)).thenReturn(Optional.of(inv));
        when(crewRepositoryPort.findBySnoForUpdate(1L)).thenReturn(Optional.of(crew));
        when(crewMemberRepositoryPort.existsByMemberSno(200L)).thenReturn(false);

        service.accept(200L, 9L);

        assertThat(inv.getStatus()).isEqualTo(CrewInvitation.STATUS_ACCEPTED);
        assertThat(crew.getMemberCount()).isEqualTo(2);
        verify(crewMemberRepositoryPort).save(any(CrewMember.class));
    }

    @Test
    void 남의_초대는_수락할_수_없다() {
        CrewInvitation inv = CrewInvitation.create(1L, 100L, 200L, LocalDateTime.now().plusDays(3));
        when(crewInvitationRepositoryPort.findBySno(9L)).thenReturn(Optional.of(inv));
        assertThatThrownBy(() -> service.accept(999L, 9L)).isInstanceOf(IllegalArgumentException.class);
        verify(crewMemberRepositoryPort, never()).save(any());
    }

    @Test
    void 만료된_초대는_수락_불가() {
        CrewInvitation inv = CrewInvitation.create(1L, 100L, 200L, LocalDateTime.now().minusDays(1));
        when(crewInvitationRepositoryPort.findBySno(9L)).thenReturn(Optional.of(inv));
        assertThatThrownBy(() -> service.accept(200L, 9L)).isInstanceOf(IllegalArgumentException.class);
        assertThat(inv.getStatus()).isEqualTo(CrewInvitation.STATUS_EXPIRED);
    }

    @Test
    void 이미_크루가_있으면_초대_수락_불가() {
        Crew crew = Crew.create("달리는곰", null, null, 30, 100L);
        CrewInvitation inv = CrewInvitation.create(1L, 100L, 200L, LocalDateTime.now().plusDays(3));
        when(crewInvitationRepositoryPort.findBySno(9L)).thenReturn(Optional.of(inv));
        when(crewRepositoryPort.findBySnoForUpdate(1L)).thenReturn(Optional.of(crew));
        when(crewMemberRepositoryPort.existsByMemberSno(200L)).thenReturn(true);

        assertThatThrownBy(() -> service.accept(200L, 9L)).isInstanceOf(IllegalArgumentException.class);
    }
}

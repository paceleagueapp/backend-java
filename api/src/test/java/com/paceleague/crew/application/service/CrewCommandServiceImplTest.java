package com.paceleague.crew.application.service;

import com.paceleague.crew.application.dto.CrewCreateRequest;
import com.paceleague.crew.application.dto.CrewUpdateRequest;
import com.paceleague.crew.application.port.out.CrewInvitationRepositoryPort;
import com.paceleague.crew.application.port.out.CrewJoinRequestRepositoryPort;
import com.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.paceleague.crew.config.CrewProperties;
import com.paceleague.crew.domain.entity.Crew;
import com.paceleague.crew.domain.entity.CrewMember;
import com.paceleague.media.application.port.in.GetApprovedMediaUrlPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CrewCommandServiceImplTest {

    @Mock CrewRepositoryPort crewRepositoryPort;
    @Mock CrewMemberRepositoryPort crewMemberRepositoryPort;
    @Mock CrewInvitationRepositoryPort crewInvitationRepositoryPort;
    @Mock CrewJoinRequestRepositoryPort crewJoinRequestRepositoryPort;
    @Mock GetApprovedMediaUrlPort getApprovedMediaUrlPort;

    CrewCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        CrewProperties props = new CrewProperties(null, null, null, null, null, null, null);
        service = new CrewCommandServiceImpl(crewRepositoryPort, crewMemberRepositoryPort,
                crewInvitationRepositoryPort, crewJoinRequestRepositoryPort, getApprovedMediaUrlPort, props);
        when(crewRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(crewMemberRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 크루_생성_시_생성자가_크루장으로_등록된다() {
        when(crewMemberRepositoryPort.existsByMemberSno(100L)).thenReturn(false);
        when(crewRepositoryPort.existsByNameAndStatusActive("달리는곰")).thenReturn(false);

        service.create(100L, new CrewCreateRequest("달리는곰", null, "우리 같이 달려요"));

        ArgumentCaptor<Crew> crew = ArgumentCaptor.forClass(Crew.class);
        verify(crewRepositoryPort).save(crew.capture());
        assertThat(crew.getValue().getLeaderMemberSno()).isEqualTo(100L);
        assertThat(crew.getValue().getMemberCount()).isEqualTo(1);

        ArgumentCaptor<CrewMember> cm = ArgumentCaptor.forClass(CrewMember.class);
        verify(crewMemberRepositoryPort).save(cm.capture());
        assertThat(cm.getValue().isLeader()).isTrue();
    }

    @Test
    void 이미_크루에_소속돼_있으면_생성_불가() {
        when(crewMemberRepositoryPort.existsByMemberSno(100L)).thenReturn(true);
        assertThatThrownBy(() -> service.create(100L, new CrewCreateRequest("새크루", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(crewRepositoryPort, never()).save(any());
    }

    @Test
    void 크루명이_중복이면_생성_불가() {
        when(crewMemberRepositoryPort.existsByMemberSno(100L)).thenReturn(false);
        when(crewRepositoryPort.existsByNameAndStatusActive("달리는곰")).thenReturn(true);
        assertThatThrownBy(() -> service.create(100L, new CrewCreateRequest("달리는곰", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 크루장이_아니면_정보_수정_불가() {
        Crew crew = Crew.create("달리는곰", null, null, 30, 100L);
        when(crewRepositoryPort.findBySno(1L)).thenReturn(Optional.of(crew));
        assertThatThrownBy(() -> service.update(200L, 1L, new CrewUpdateRequest("바뀐이름", null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 크루원이_남아있으면_해체_불가() {
        Crew crew = Crew.create("달리는곰", null, null, 30, 100L);
        crew.increaseMemberCount(); // 2명
        when(crewRepositoryPort.findBySnoForUpdate(1L)).thenReturn(Optional.of(crew));
        assertThatThrownBy(() -> service.disband(100L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(crewRepositoryPort, never()).delete(any());
    }

    @Test
    void 크루장_위임_시_역할이_뒤바뀐다() {
        Crew crew = Crew.create("달리는곰", null, null, 30, 100L);
        CrewMember leader = CrewMember.leader(1L, 100L);
        CrewMember member = CrewMember.member(1L, 200L);
        when(crewRepositoryPort.findBySno(1L)).thenReturn(Optional.of(crew));
        when(crewMemberRepositoryPort.findByCrewSnoAndMemberSno(1L, 100L)).thenReturn(Optional.of(leader));
        when(crewMemberRepositoryPort.findByCrewSnoAndMemberSno(1L, 200L)).thenReturn(Optional.of(member));

        service.transferLeader(100L, 1L, 200L);

        assertThat(leader.isLeader()).isFalse();
        assertThat(member.isLeader()).isTrue();
        assertThat(crew.getLeaderMemberSno()).isEqualTo(200L);
    }
}

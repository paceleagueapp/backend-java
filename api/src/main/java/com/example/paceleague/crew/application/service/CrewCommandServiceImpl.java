package com.example.paceleague.crew.application.service;

import com.example.paceleague.crew.application.dto.CrewCreateRequest;
import com.example.paceleague.crew.application.dto.CrewUpdateRequest;
import com.example.paceleague.crew.application.port.in.CrewCommandUseCase;
import com.example.paceleague.crew.application.port.out.CrewInvitationRepositoryPort;
import com.example.paceleague.crew.application.port.out.CrewJoinRequestRepositoryPort;
import com.example.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.example.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.example.paceleague.crew.config.CrewProperties;
import com.example.paceleague.crew.domain.entity.Crew;
import com.example.paceleague.crew.domain.entity.CrewMember;
import com.example.paceleague.crew.domain.policy.CrewMembershipPolicy;
import com.example.paceleague.crew.domain.policy.CrewNamePolicy;
import com.example.paceleague.media.application.port.in.GetApprovedMediaUrlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewCommandServiceImpl implements CrewCommandUseCase {

    private final CrewRepositoryPort crewRepositoryPort;
    private final CrewMemberRepositoryPort crewMemberRepositoryPort;
    private final CrewInvitationRepositoryPort crewInvitationRepositoryPort;
    private final CrewJoinRequestRepositoryPort crewJoinRequestRepositoryPort;
    private final GetApprovedMediaUrlPort getApprovedMediaUrlPort;
    private final CrewProperties props;

    @Override
    public Long create(Long memberSno, CrewCreateRequest req) {
        if (crewMemberRepositoryPort.existsByMemberSno(memberSno)) {
            throw new IllegalArgumentException("이미 크루에 소속되어 있습니다. 새 크루를 만들려면 먼저 탈퇴하세요");
        }
        String name = CrewNamePolicy.normalizeAndValidate(req.name(), props.nameMinLength(), props.nameMaxLength());
        if (crewRepositoryPort.existsByNameAndStatusActive(name)) {
            throw new IllegalArgumentException("이미 사용 중인 크루명입니다");
        }
        String iconUrl = resolveIcon(req.iconMediaId(), memberSno);
        String description = trimToMax(req.description(), props.descriptionMaxLength());

        Crew crew = crewRepositoryPort.save(
                Crew.create(name, iconUrl, description, props.memberLimitDefault(), memberSno));
        crewMemberRepositoryPort.save(CrewMember.leader(crew.getSno(), memberSno));
        return crew.getSno();
    }

    @Override
    public void update(Long memberSno, Long crewSno, CrewUpdateRequest req) {
        Crew crew = getCrew(crewSno);
        CrewMembershipPolicy.assertLeader(crew, memberSno);

        String name = CrewNamePolicy.normalizeAndValidate(req.name(), props.nameMinLength(), props.nameMaxLength());
        if (!name.equals(crew.getName()) && crewRepositoryPort.existsByNameAndStatusActive(name)) {
            throw new IllegalArgumentException("이미 사용 중인 크루명입니다");
        }
        String iconUrl;
        if (req.iconMediaId() != null) {
            iconUrl = resolveIcon(req.iconMediaId(), memberSno);
        } else if (req.iconUrl() != null && !req.iconUrl().isBlank()) {
            iconUrl = req.iconUrl().trim(); // 아이콘 변경 없음 — 클라이언트가 현재 URL을 그대로 돌려보낸 것
        } else {
            iconUrl = null; // 아이콘 제거
        }
        crew.updateInfo(name, iconUrl, trimToMax(req.description(), props.descriptionMaxLength()));
        crew.updateNotice(trimToMax(req.notice(), props.noticeMaxLength()));
        crewRepositoryPort.save(crew);
    }

    @Override
    public void disband(Long memberSno, Long crewSno) {
        Crew crew = crewRepositoryPort.findBySnoForUpdate(crewSno)
                .orElseThrow(CrewCommandServiceImpl::crewNotFound);
        CrewMembershipPolicy.assertLeader(crew, memberSno);
        CrewMembershipPolicy.assertDisbandable(crew);

        // 연쇄 삭제 (JPA cascade 안 씀 — 코드베이스 컨벤션). 크루 자체는 하드 삭제(크루명 재사용 가능).
        crewInvitationRepositoryPort.deleteByCrewSno(crewSno);
        crewJoinRequestRepositoryPort.deleteByCrewSno(crewSno);
        crewMemberRepositoryPort.deleteByCrewSno(crewSno);
        crewRepositoryPort.delete(crew);
    }

    @Override
    public void leave(Long memberSno, Long crewSno) {
        Crew crew = crewRepositoryPort.findBySnoForUpdate(crewSno)
                .orElseThrow(CrewCommandServiceImpl::crewNotFound);
        CrewMember cm = crewMemberRepositoryPort.findByCrewSnoAndMemberSno(crewSno, memberSno)
                .orElseThrow(() -> new IllegalArgumentException("크루원이 아닙니다"));
        CrewMembershipPolicy.assertLeaderCanLeave(crew, memberSno);

        crewMemberRepositoryPort.delete(cm);
        crew.decreaseMemberCount();
        crewRepositoryPort.save(crew);
    }

    @Override
    public void kick(Long leaderMemberSno, Long crewSno, Long targetMemberSno) {
        Crew crew = crewRepositoryPort.findBySnoForUpdate(crewSno)
                .orElseThrow(CrewCommandServiceImpl::crewNotFound);
        CrewMembershipPolicy.assertLeader(crew, leaderMemberSno);
        if (leaderMemberSno.equals(targetMemberSno)) {
            throw new IllegalArgumentException("크루장은 스스로를 추방할 수 없습니다");
        }
        CrewMember target = crewMemberRepositoryPort.findByCrewSnoAndMemberSno(crewSno, targetMemberSno)
                .orElseThrow(() -> new IllegalArgumentException("대상이 크루원이 아닙니다"));

        crewMemberRepositoryPort.delete(target);
        crew.decreaseMemberCount();
        crewRepositoryPort.save(crew);
    }

    @Override
    public void transferLeader(Long leaderMemberSno, Long crewSno, Long targetMemberSno) {
        Crew crew = getCrew(crewSno);
        CrewMembershipPolicy.assertLeader(crew, leaderMemberSno);
        if (leaderMemberSno.equals(targetMemberSno)) {
            throw new IllegalArgumentException("이미 크루장입니다");
        }
        CrewMember current = crewMemberRepositoryPort.findByCrewSnoAndMemberSno(crewSno, leaderMemberSno)
                .orElseThrow(() -> new IllegalArgumentException("크루원이 아닙니다"));
        CrewMember target = crewMemberRepositoryPort.findByCrewSnoAndMemberSno(crewSno, targetMemberSno)
                .orElseThrow(() -> new IllegalArgumentException("대상이 크루원이 아닙니다"));

        current.demoteToMember();
        target.promoteToLeader();
        crewMemberRepositoryPort.save(current);
        crewMemberRepositoryPort.save(target);
        crew.changeLeader(targetMemberSno);
        crewRepositoryPort.save(crew);
    }

    private Crew getCrew(Long crewSno) {
        return crewRepositoryPort.findBySno(crewSno).orElseThrow(CrewCommandServiceImpl::crewNotFound);
    }

    private static IllegalArgumentException crewNotFound() {
        return new IllegalArgumentException("크루를 찾을 수 없습니다");
    }

    private String resolveIcon(Long iconMediaId, Long ownerMemberSno) {
        return iconMediaId == null ? null : getApprovedMediaUrlPort.requireApprovedUrl(iconMediaId, ownerMemberSno);
    }

    private static String trimToMax(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > max) {
            throw new IllegalArgumentException("길이 제한(" + max + "자)을 초과했습니다");
        }
        return trimmed;
    }
}

package com.paceleague.crew.adapter.out.persistence;

import com.paceleague.crew.application.port.in.shared.GetMemberCrewBadgePort.CrewBadge;
import com.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.paceleague.crew.domain.entity.CrewMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CrewMemberPersistenceAdapter implements CrewMemberRepositoryPort {

    private final CrewMemberJpaRepository crewMemberJpaRepository;

    public CrewMember save(CrewMember crewMember) {
        return crewMemberJpaRepository.save(crewMember);
    }

    public Optional<CrewMember> findByMemberSno(Long memberSno) {
        return crewMemberJpaRepository.findByMemberSno(memberSno);
    }

    public Optional<CrewMember> findByCrewSnoAndMemberSno(Long crewSno, Long memberSno) {
        return crewMemberJpaRepository.findByCrewSnoAndMemberSno(crewSno, memberSno);
    }

    public List<CrewMember> findByCrewSnoOrderByJoinedAtAsc(Long crewSno) {
        return crewMemberJpaRepository.findByCrewSnoOrderByJoinedAtAsc(crewSno);
    }

    public boolean existsByMemberSno(Long memberSno) {
        return crewMemberJpaRepository.existsByMemberSno(memberSno);
    }

    public Map<Long, CrewBadge> findBadgesByMemberSnos(Collection<Long> memberSnos) {
        if (memberSnos == null || memberSnos.isEmpty()) {
            return Map.of();
        }
        Map<Long, CrewBadge> result = new LinkedHashMap<>();
        for (CrewMemberBadgeProjection p : crewMemberJpaRepository.findBadgesByMemberSnoIn(memberSnos)) {
            result.put(p.getMemberSno(), new CrewBadge(p.getCrewName(), p.getCrewIconUrl()));
        }
        return result;
    }

    public void delete(CrewMember crewMember) {
        crewMemberJpaRepository.delete(crewMember);
    }

    public void deleteByCrewSno(Long crewSno) {
        crewMemberJpaRepository.deleteByCrewSno(crewSno);
    }
}

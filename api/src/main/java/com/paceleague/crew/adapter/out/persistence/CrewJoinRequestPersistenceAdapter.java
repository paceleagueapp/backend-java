package com.paceleague.crew.adapter.out.persistence;

import com.paceleague.crew.application.port.out.CrewJoinRequestRepositoryPort;
import com.paceleague.crew.domain.entity.CrewJoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CrewJoinRequestPersistenceAdapter implements CrewJoinRequestRepositoryPort {

    private final CrewJoinRequestJpaRepository crewJoinRequestJpaRepository;

    public CrewJoinRequest save(CrewJoinRequest joinRequest) {
        return crewJoinRequestJpaRepository.save(joinRequest);
    }

    public Optional<CrewJoinRequest> findBySno(Long sno) {
        return crewJoinRequestJpaRepository.findById(sno);
    }

    public List<CrewJoinRequest> findPendingByCrewSno(Long crewSno) {
        return crewJoinRequestJpaRepository.findByCrewSnoAndStatusOrderByCreateAtAsc(
                crewSno, CrewJoinRequest.STATUS_PENDING);
    }

    public boolean existsPendingByCrewSnoAndMemberSno(Long crewSno, Long memberSno) {
        return crewJoinRequestJpaRepository.existsByCrewSnoAndMemberSnoAndStatus(
                crewSno, memberSno, CrewJoinRequest.STATUS_PENDING);
    }

    public void deleteByCrewSno(Long crewSno) {
        crewJoinRequestJpaRepository.deleteByCrewSno(crewSno);
    }
}

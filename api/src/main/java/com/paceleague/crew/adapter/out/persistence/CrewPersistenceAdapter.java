package com.paceleague.crew.adapter.out.persistence;

import com.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.paceleague.crew.domain.entity.Crew;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CrewPersistenceAdapter implements CrewRepositoryPort {

    private final CrewJpaRepository crewJpaRepository;

    public Crew save(Crew crew) {
        return crewJpaRepository.save(crew);
    }

    public Optional<Crew> findBySno(Long sno) {
        return crewJpaRepository.findById(sno);
    }

    public Optional<Crew> findBySnoForUpdate(Long sno) {
        return crewJpaRepository.findBySnoForUpdate(sno);
    }

    public boolean existsByNameAndStatusActive(String name) {
        return crewJpaRepository.existsByNameAndStatus(name, Crew.STATUS_ACTIVE);
    }

    public List<Crew> searchActiveByName(String q, int limit) {
        return crewJpaRepository.searchActive(q == null ? "" : q.trim(), PageRequest.of(0, limit));
    }

    public void delete(Crew crew) {
        crewJpaRepository.delete(crew);
    }
}

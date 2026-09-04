package com.paceleague.crew.application.port.out;

import com.paceleague.crew.domain.entity.Crew;

import java.util.List;
import java.util.Optional;

public interface CrewRepositoryPort {

    Crew save(Crew crew);

    Optional<Crew> findBySno(Long sno);

    // 가입 확정(초대 수락/신청 승인) 시 인원수 갱신을 직렬화하기 위한 비관적 락.
    Optional<Crew> findBySnoForUpdate(Long sno);

    boolean existsByNameAndStatusActive(String name);

    // 크루명 검색(ACTIVE만). q 가 blank면 전체(제한). 이름 오름차순.
    List<Crew> searchActiveByName(String q, int limit);

    void delete(Crew crew);
}

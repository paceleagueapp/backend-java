package com.paceleague.crew.application.port.out;

import com.paceleague.crew.domain.entity.CrewJoinRequest;

import java.util.List;
import java.util.Optional;

public interface CrewJoinRequestRepositoryPort {

    CrewJoinRequest save(CrewJoinRequest joinRequest);

    Optional<CrewJoinRequest> findBySno(Long sno);

    // 특정 크루로 온 PENDING 신청(신청 순).
    List<CrewJoinRequest> findPendingByCrewSno(Long crewSno);

    boolean existsPendingByCrewSnoAndMemberSno(Long crewSno, Long memberSno);

    void deleteByCrewSno(Long crewSno);

    // 회원 탈퇴 시 그 회원의 가입신청 전부 삭제.
    void deleteAllByMemberSno(Long memberSno);
}

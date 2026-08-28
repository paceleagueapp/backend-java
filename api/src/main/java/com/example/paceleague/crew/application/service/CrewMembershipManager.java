package com.example.paceleague.crew.application.service;

import com.example.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.example.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.example.paceleague.crew.domain.entity.Crew;
import com.example.paceleague.crew.domain.entity.CrewMember;
import com.example.paceleague.crew.domain.policy.CrewMembershipPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 초대 수락 / 가입신청 승인 양쪽에서 쓰는 "크루 가입 확정" 로직. 크루 row 비관적 락으로 정원/중복소속을 직렬화한다.
@Component
@RequiredArgsConstructor
class CrewMembershipManager {

    private final CrewRepositoryPort crewRepositoryPort;
    private final CrewMemberRepositoryPort crewMemberRepositoryPort;

    void joinCrew(Long crewSno, Long memberSno) {
        Crew crew = crewRepositoryPort.findBySnoForUpdate(crewSno)
                .orElseThrow(() -> new IllegalArgumentException("크루를 찾을 수 없습니다"));
        boolean alreadyInACrew = crewMemberRepositoryPort.existsByMemberSno(memberSno);
        CrewMembershipPolicy.assertJoinable(crew, alreadyInACrew);

        crewMemberRepositoryPort.save(CrewMember.member(crewSno, memberSno));
        crew.increaseMemberCount();
        crewRepositoryPort.save(crew);
    }
}

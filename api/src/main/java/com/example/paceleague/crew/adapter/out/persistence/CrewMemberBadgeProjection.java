package com.example.paceleague.crew.adapter.out.persistence;

// crew_member ⋈ crew 조인 결과 — board/ranking 크루 배지용 배치 조회.
public interface CrewMemberBadgeProjection {
    Long getMemberSno();

    String getCrewName();

    String getCrewIconUrl();
}

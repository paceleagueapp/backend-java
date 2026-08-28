package com.example.paceleague.crew.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

// 다른 도메인(board/ranking)이 "이 회원이 어느 크루 소속인지"를 표시하기 위한 크로스 도메인 포트.
// 크루 없는 회원은 결과에서 빠진다.
public interface GetMemberCrewBadgePort {

    Optional<CrewBadge> getBadge(Long memberSno);

    Map<Long, CrewBadge> getBadges(Collection<Long> memberSnos);

    record CrewBadge(String crewName, String crewIconUrl) {
    }
}

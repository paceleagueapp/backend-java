package com.paceleague.member.application.port.in.shared;

import java.util.Set;

// board 등 다른 도메인이 "이 조회자가 차단한 회원 목록" 을 얻기 위한 크로스 도메인 포트.
public interface GetBlockedMemberSnosPort {
    Set<Long> getBlockedBy(Long viewerMemberSno);
}

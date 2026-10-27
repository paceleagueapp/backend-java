package com.paceleague.member.application.port.in.shared;

import java.time.Instant;
import java.util.Optional;

// 회원 가입 시각 조회 — notification 이 "가입 이후 전체 발송분만" 알림내역에 보여주기 위해 사용.
public interface GetMemberJoinedAtPort {

    Optional<Instant> getJoinedAt(Long memberSno);
}

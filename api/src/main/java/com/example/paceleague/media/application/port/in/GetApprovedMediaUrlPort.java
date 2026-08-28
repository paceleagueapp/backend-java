package com.example.paceleague.media.application.port.in;

// 크루 아이콘 등 "이미 모더레이션 통과한 이미지 URL"을 다른 도메인이 가져다 쓰기 위한 크로스 도메인 포트.
public interface GetApprovedMediaUrlPort {

    // 본인 소유이면서 APPROVED 상태인 media의 url. 아니면 IllegalArgumentException(→ 400).
    String requireApprovedUrl(Long mediaSno, Long ownerMemberSno);
}

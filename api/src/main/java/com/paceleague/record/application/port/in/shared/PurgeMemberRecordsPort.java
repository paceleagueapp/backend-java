package com.paceleague.record.application.port.in.shared;

// 회원 탈퇴 시 그 회원의 러닝 기록·GPS 트랙을 전부 삭제한다.
public interface PurgeMemberRecordsPort {
    void purge(Long memberSno);
}

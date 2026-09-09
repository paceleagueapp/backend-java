package com.paceleague.media.application.port.in.shared;

// 회원 탈퇴 시 그 회원이 업로드한 미디어 행을 삭제한다(S3 객체 정리는 후순위).
public interface PurgeMemberMediaPort {
    void purge(Long memberSno);
}

package com.paceleague.board.application.port.in.shared;

// 회원 탈퇴 시 그 회원의 추천/신고 이력을 삭제한다(글·댓글 본문은 익명화 유지).
public interface PurgeMemberBoardPort {
    void purge(Long memberSno);
}

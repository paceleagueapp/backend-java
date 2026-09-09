package com.paceleague.board.application.service;

import com.paceleague.board.application.port.in.shared.PurgeMemberBoardPort;
import com.paceleague.board.application.port.out.CommentVoteRepositoryPort;
import com.paceleague.board.application.port.out.PostVoteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardPurgeService implements PurgeMemberBoardPort {

    private final PostVoteRepositoryPort postVoteRepositoryPort;
    private final CommentVoteRepositoryPort commentVoteRepositoryPort;

    @Override
    @Transactional
    public void purge(Long memberSno) {
        postVoteRepositoryPort.deleteAllByMemberSno(memberSno);
        commentVoteRepositoryPort.deleteAllByMemberSno(memberSno);
        // board_report(신고자=탈퇴자) 삭제는 신고 기능(Part B)에서 이 메서드에 추가.
    }
}

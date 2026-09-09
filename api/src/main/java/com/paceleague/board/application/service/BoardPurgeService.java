package com.paceleague.board.application.service;

import com.paceleague.board.application.port.in.shared.PurgeMemberBoardPort;
import com.paceleague.board.application.port.out.BoardReportRepositoryPort;
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
    private final BoardReportRepositoryPort boardReportRepositoryPort;

    @Override
    @Transactional
    public void purge(Long memberSno) {
        postVoteRepositoryPort.deleteAllByMemberSno(memberSno);
        commentVoteRepositoryPort.deleteAllByMemberSno(memberSno);
        // 탈퇴자가 낸 신고는 삭제(그 신고로 이미 숨겨진 글의 hidden 상태는 유지된다).
        boardReportRepositoryPort.deleteAllByReporter(memberSno);
    }
}

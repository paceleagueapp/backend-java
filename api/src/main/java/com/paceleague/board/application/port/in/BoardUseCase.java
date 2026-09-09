package com.paceleague.board.application.port.in;

import com.paceleague.board.application.dto.CommentCreateRequest;
import com.paceleague.board.application.dto.PostCreateRequest;
import com.paceleague.board.application.dto.VoteResponse;

public interface BoardUseCase {
    Long createPost(Long memberSno, Long boardSno, PostCreateRequest req);

    void updatePost(Long memberSno, Long postSno, PostCreateRequest req);

    void deletePost(Long memberSno, Long postSno);

    VoteResponse votePost(Long memberSno, Long postSno, int voteValue);

    Long createComment(Long memberSno, Long postSno, CommentCreateRequest req);

    void deleteComment(Long memberSno, Long commentSno);

    VoteResponse voteComment(Long memberSno, Long commentSno, int voteValue);

    // 신고 — reason 은 ReportReason enum 이름. 본인 글/댓글은 신고 불가, 중복 신고는 멱등.
    // 서로 다른 신고자가 임계값에 도달하면 대상이 자동 숨김된다.
    void reportPost(Long memberSno, Long postSno, String reason, String detail);

    void reportComment(Long memberSno, Long commentSno, String reason, String detail);
}

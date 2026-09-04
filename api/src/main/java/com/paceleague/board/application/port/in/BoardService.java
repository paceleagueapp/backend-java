package com.paceleague.board.application.port.in;

import com.paceleague.board.application.dto.CommentCreateRequest;
import com.paceleague.board.application.dto.PostCreateRequest;
import com.paceleague.board.application.dto.VoteResponse;

public interface BoardService {
    Long createPost(Long memberSno, Long boardSno, PostCreateRequest req);

    void updatePost(Long memberSno, Long postSno, PostCreateRequest req);

    void deletePost(Long memberSno, Long postSno);

    VoteResponse votePost(Long memberSno, Long postSno, int voteValue);

    Long createComment(Long memberSno, Long postSno, CommentCreateRequest req);

    void deleteComment(Long memberSno, Long commentSno);

    VoteResponse voteComment(Long memberSno, Long commentSno, int voteValue);
}

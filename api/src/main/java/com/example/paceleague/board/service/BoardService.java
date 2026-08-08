package com.example.paceleague.board.service;

import com.example.paceleague.board.dto.CommentCreateRequest;
import com.example.paceleague.board.dto.PostCreateRequest;
import com.example.paceleague.board.dto.VoteResponse;

public interface BoardService {
    Long createPost(Long memberSno, Long boardSno, PostCreateRequest req);

    void deletePost(Long memberSno, Long postSno);

    VoteResponse votePost(Long memberSno, Long postSno, int voteValue);

    Long createComment(Long memberSno, Long postSno, CommentCreateRequest req);

    void deleteComment(Long memberSno, Long commentSno);

    VoteResponse voteComment(Long memberSno, Long commentSno, int voteValue);
}

package com.example.paceleague.board.application.port.in;

import com.example.paceleague.board.application.dto.BoardResponse;
import com.example.paceleague.board.application.dto.CommentResponse;
import com.example.paceleague.board.application.dto.PostDetailResponse;
import com.example.paceleague.board.application.dto.PostSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BoardQueryService {
    List<BoardResponse> listBoards();

    Page<PostSummaryResponse> listPosts(Long boardSno, int page, int size, String sort);

    // memberSno가 null이면 비로그인 조회 — myVote는 항상 null로 반환된다.
    PostDetailResponse getPost(Long memberSno, Long postSno);

    List<CommentResponse> listComments(Long memberSno, Long postSno);
}

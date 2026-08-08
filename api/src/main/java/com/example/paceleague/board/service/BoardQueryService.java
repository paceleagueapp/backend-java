package com.example.paceleague.board.service;

import com.example.paceleague.board.dto.BoardResponse;
import com.example.paceleague.board.dto.CommentResponse;
import com.example.paceleague.board.dto.PostDetailResponse;
import com.example.paceleague.board.dto.PostSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BoardQueryService {
    List<BoardResponse> listBoards();

    Page<PostSummaryResponse> listPosts(Long boardSno, int page, int size, String sort);

    PostDetailResponse getPost(Long memberSno, Long postSno);

    List<CommentResponse> listComments(Long memberSno, Long postSno);
}

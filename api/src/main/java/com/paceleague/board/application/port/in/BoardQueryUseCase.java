package com.paceleague.board.application.port.in;

import com.paceleague.board.application.dto.BoardResponse;
import com.paceleague.board.application.dto.CommentResponse;
import com.paceleague.board.application.dto.PostDetailResponse;
import com.paceleague.board.application.dto.PostSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BoardQueryUseCase {
    // lang: 보드명/티어뱃지 등 정적 UI 라벨의 표시 언어(ko/en/ja/zh/es/fr/de/pt/vi/th), 미지원 값이면 ko로 처리된다.
    List<BoardResponse> listBoards(String lang);

    // viewerMemberSno: 비로그인이면 null. 차단한 작성자의 글을 목록에서 제외하는 데 쓰인다.
    Page<PostSummaryResponse> listPosts(Long viewerMemberSno, Long boardSno, int page, int size, String sort, String lang);

    // memberSno가 null이면 비로그인 조회 — myVote는 항상 null로 반환된다.
    PostDetailResponse getPost(Long memberSno, Long postSno, String lang);

    List<CommentResponse> listComments(Long memberSno, Long postSno);
}

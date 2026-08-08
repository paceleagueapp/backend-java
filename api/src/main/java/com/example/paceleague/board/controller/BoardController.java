package com.example.paceleague.board.controller;

import com.example.paceleague.board.dto.*;
import com.example.paceleague.board.service.BoardQueryService;
import com.example.paceleague.board.service.BoardService;
import com.example.paceleague.board.service.TranslationService;
import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.security.JwtAuthenticationFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@Tag(name = "Board", description = "커뮤니티(게시판) API — 보드/게시글/댓글/추천. 조회(GET)는 비로그인도 가능, 작성/삭제/추천은 로그인 필요.")
public class BoardController {
    private final BoardService boardService;
    private final BoardQueryService boardQueryService;
    private final TranslationService translationService;

    public BoardController(BoardService boardService, BoardQueryService boardQueryService, TranslationService translationService) {
        this.boardService = boardService;
        this.boardQueryService = boardQueryService;
        this.translationService = translationService;
    }

    @Operation(summary = "보드 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseApi<List<BoardResponse>> listBoards() {
        return ResponseApi.success(boardQueryService.listBoards());
    }

    @Operation(summary = "게시글 목록 조회", description = "sort=new(기본)|top")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{boardSno}/posts")
    public ResponseApi<Page<PostSummaryResponse>> listPosts(
            @PathVariable Long boardSno,
            @Parameter(description = "페이지 번호(0-base)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "new") String sort
    ) {
        return ResponseApi.success(boardQueryService.listPosts(boardSno, page, size, sort));
    }

    @Operation(summary = "게시글 작성")
    @ApiResponse(responseCode = "200", description = "작성 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{boardSno}/posts")
    public ResponseEntity<ResponseApi<CreatedResponse>> createPost(
            Authentication authentication,
            @PathVariable Long boardSno,
            @RequestBody PostCreateRequest req
    ) {
        Long sno = boardService.createPost(uno(authentication), boardSno, req);
        return ResponseEntity.ok(ResponseApi.success("게시글이 작성되었습니다.", new CreatedResponse(sno)));
    }

    @Operation(summary = "게시글 상세 조회", description = "조회 시 조회수가 1 증가합니다. 비로그인 시에도 조회 가능(이 경우 myVote는 항상 null).")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/posts/{postSno}")
    public ResponseApi<PostDetailResponse> getPost(Authentication authentication, @PathVariable Long postSno) {
        return ResponseApi.success(boardQueryService.getPost(unoOrNull(authentication), postSno));
    }

    @Operation(summary = "게시글 삭제", description = "본인 게시글만 삭제 가능하며, 댓글/추천도 함께 삭제됩니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/posts/{postSno}")
    public ResponseApi<String> deletePost(Authentication authentication, @PathVariable Long postSno) {
        boardService.deletePost(uno(authentication), postSno);
        return ResponseApi.success("게시글이 삭제되었습니다.");
    }

    @Operation(summary = "게시글 번역", description = "제목/본문을 대상 언어로 번역합니다. 외부 번역 API(AWS Translate) 호출 비용 통제를 위해 조회성 동작이지만 로그인이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "번역 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/posts/{postSno}/translate")
    public ResponseApi<PostTranslationResponse> translatePost(
            @PathVariable Long postSno,
            @RequestBody TranslateRequest req
    ) {
        return ResponseApi.success(translationService.translatePost(postSno, req.targetLanguage()));
    }

    @Operation(summary = "게시글 추천/비추천", description = "같은 값 재요청 시 취소, 다른 값이면 전환됩니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/posts/{postSno}/vote")
    public ResponseApi<VoteResponse> votePost(
            Authentication authentication,
            @PathVariable Long postSno,
            @RequestBody VoteRequest req
    ) {
        return ResponseApi.success(boardService.votePost(uno(authentication), postSno, req.voteValue()));
    }

    @Operation(summary = "댓글 목록 조회", description = "최상위 댓글과 그 대댓글(1단계)만 포함합니다. 비로그인 시에도 조회 가능(이 경우 myVote는 항상 null).")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/posts/{postSno}/comments")
    public ResponseApi<List<CommentResponse>> listComments(Authentication authentication, @PathVariable Long postSno) {
        return ResponseApi.success(boardQueryService.listComments(unoOrNull(authentication), postSno));
    }

    @Operation(summary = "댓글/대댓글 작성", description = "parentCommentSno를 주면 대댓글로 작성됩니다. 대댓글에는 답글을 달 수 없습니다.")
    @ApiResponse(responseCode = "200", description = "작성 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/posts/{postSno}/comments")
    public ResponseEntity<ResponseApi<CreatedResponse>> createComment(
            Authentication authentication,
            @PathVariable Long postSno,
            @RequestBody CommentCreateRequest req
    ) {
        Long sno = boardService.createComment(uno(authentication), postSno, req);
        return ResponseEntity.ok(ResponseApi.success("댓글이 작성되었습니다.", new CreatedResponse(sno)));
    }

    @Operation(summary = "댓글 번역", description = "댓글 내용을 대상 언어로 번역합니다. 로그인 필요(사유는 게시글 번역과 동일).")
    @ApiResponse(responseCode = "200", description = "번역 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/comments/{commentSno}/translate")
    public ResponseApi<CommentTranslationResponse> translateComment(
            @PathVariable Long commentSno,
            @RequestBody TranslateRequest req
    ) {
        return ResponseApi.success(translationService.translateComment(commentSno, req.targetLanguage()));
    }

    @Operation(summary = "댓글 삭제", description = "본인 댓글만 삭제 가능하며, 대댓글/추천도 함께 삭제됩니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/comments/{commentSno}")
    public ResponseApi<String> deleteComment(Authentication authentication, @PathVariable Long commentSno) {
        boardService.deleteComment(uno(authentication), commentSno);
        return ResponseApi.success("댓글이 삭제되었습니다.");
    }

    @Operation(summary = "댓글 추천/비추천")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/comments/{commentSno}/vote")
    public ResponseApi<VoteResponse> voteComment(
            Authentication authentication,
            @PathVariable Long commentSno,
            @RequestBody VoteRequest req
    ) {
        return ResponseApi.success(boardService.voteComment(uno(authentication), commentSno, req.voteValue()));
    }

    public record CreatedResponse(Long sno) {}

    private long uno(Authentication authentication) {
        var p = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();
        return p.memberSno();
    }

    // 조회(GET) 엔드포인트는 비로그인도 허용되므로, 로그인 상태가 아니면 null을 반환한다.
    private Long unoOrNull(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticationFilter.AuthPrincipal p)) {
            return null;
        }
        return p.memberSno();
    }
}

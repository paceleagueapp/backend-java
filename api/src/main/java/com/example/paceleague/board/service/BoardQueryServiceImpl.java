package com.example.paceleague.board.service;

import com.example.paceleague.board.dto.BoardResponse;
import com.example.paceleague.board.dto.CommentResponse;
import com.example.paceleague.board.dto.PostDetailResponse;
import com.example.paceleague.board.dto.PostSummaryResponse;
import com.example.paceleague.board.entity.Board;
import com.example.paceleague.board.entity.Comment;
import com.example.paceleague.board.entity.Post;
import com.example.paceleague.board.repository.BoardRepository;
import com.example.paceleague.board.repository.CommentRepository;
import com.example.paceleague.board.repository.CommentVoteRepository;
import com.example.paceleague.board.repository.PostRepository;
import com.example.paceleague.board.repository.PostVoteRepository;
import com.example.paceleague.member.entity.Member;
import com.example.paceleague.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardQueryServiceImpl implements BoardQueryService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final MemberRepository memberRepository;

    public List<BoardResponse> listBoards() {
        return boardRepository.findAllByOrderByDisplayOrderAsc()
                .stream().map(BoardResponse::from).toList();
    }

    public Page<PostSummaryResponse> listPosts(Long boardSno, int page, int size, String sort) {
        boardRepository.findById(boardSno)
                .orElseThrow(() -> new IllegalArgumentException("board not found"));

        int pageSize = (size <= 0) ? 20 : size;
        Sort sortOrder = "top".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "score").and(Sort.by(Sort.Direction.DESC, "createAt"))
                : Sort.by(Sort.Direction.DESC, "createAt");

        var pageable = PageRequest.of(Math.max(page, 0), pageSize, sortOrder);

        return postRepository.findByBoardSno(boardSno, pageable)
                .map(post -> PostSummaryResponse.from(post, nicknameOf(post.getMemberSno()), commentRepository.countByPostSno(post.getSno())));
    }

    @Transactional
    public PostDetailResponse getPost(Long memberSno, Long postSno) {
        Post post = postRepository.findById(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        postRepository.incrementViewCount(postSno);

        Board board = boardRepository.findById(post.getBoardSno())
                .orElseThrow(() -> new IllegalArgumentException("board not found"));

        Integer myVote = postVoteRepository.findByPostSnoAndMemberSno(postSno, memberSno)
                .map(vote -> vote.getVoteValue())
                .orElse(null);

        // post.getViewCount()는 위 incrementViewCount()가 반영되기 전 값이므로 +1 해서 응답한다.
        return new PostDetailResponse(
                post.getSno(), post.getBoardSno(), board.getName(), post.getTitle(), post.getContent(),
                post.getMemberSno(), nicknameOf(post.getMemberSno()), post.getViewCount() + 1, post.getScore(), myVote,
                post.getCreateAt(), post.getUpdateAt()
        );
    }

    public List<CommentResponse> listComments(Long memberSno, Long postSno) {
        if (!postRepository.existsById(postSno)) {
            throw new IllegalArgumentException("post not found");
        }

        List<Comment> all = commentRepository.findByPostSnoOrderByCreateAtAsc(postSno);

        Map<Long, List<Comment>> repliesByParent = all.stream()
                .filter(Comment::isReply)
                .collect(Collectors.groupingBy(Comment::getParentCommentSno));

        return all.stream()
                .filter(comment -> !comment.isReply())
                .map(comment -> toResponse(comment, memberSno, repliesByParent.getOrDefault(comment.getSno(), List.of())
                        .stream().map(reply -> toResponse(reply, memberSno, List.of())).toList()))
                .toList();
    }

    private CommentResponse toResponse(Comment comment, Long memberSno, List<CommentResponse> replies) {
        Integer myVote = commentVoteRepository.findByCommentSnoAndMemberSno(comment.getSno(), memberSno)
                .map(vote -> vote.getVoteValue())
                .orElse(null);
        return CommentResponse.from(comment, nicknameOf(comment.getMemberSno()), myVote, replies);
    }

    private String nicknameOf(Long memberSno) {
        return memberRepository.findBySno(memberSno)
                .map(Member::getNickname)
                .orElse("알 수 없음");
    }
}

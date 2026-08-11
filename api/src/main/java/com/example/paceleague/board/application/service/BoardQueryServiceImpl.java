package com.example.paceleague.board.application.service;

import com.example.paceleague.board.application.dto.BoardResponse;
import com.example.paceleague.board.application.dto.CommentResponse;
import com.example.paceleague.board.application.dto.PostDetailResponse;
import com.example.paceleague.board.application.dto.PostSummaryResponse;
import com.example.paceleague.board.application.port.in.BoardQueryService;
import com.example.paceleague.board.application.port.out.BoardRepositoryPort;
import com.example.paceleague.board.application.port.out.CommentRepositoryPort;
import com.example.paceleague.board.application.port.out.CommentVoteRepositoryPort;
import com.example.paceleague.board.application.port.out.PostRepositoryPort;
import com.example.paceleague.board.application.port.out.PostVoteRepositoryPort;
import com.example.paceleague.board.domain.entity.Board;
import com.example.paceleague.board.domain.entity.Comment;
import com.example.paceleague.board.domain.entity.Post;
import com.example.paceleague.board.domain.policy.BoardLabelPolicy;
import com.example.paceleague.common.i18n.Language;
import com.example.paceleague.media.application.dto.MediaAttachmentResponse;
import com.example.paceleague.media.application.port.in.GetPostAttachmentsPort;
import com.example.paceleague.member.application.port.in.GetMemberNicknamePort;
import com.example.paceleague.rank.application.port.in.GetMemberTierPort;
import com.example.paceleague.rank.domain.enums.RankTier;
import com.example.paceleague.rank.domain.policy.RankTierLabelPolicy;
import com.example.paceleague.record.application.dto.RunningRecordResponse;
import com.example.paceleague.record.application.port.in.GetRecordSummaryPort;
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

    private final BoardRepositoryPort boardRepositoryPort;
    private final PostRepositoryPort postRepositoryPort;
    private final CommentRepositoryPort commentRepositoryPort;
    private final PostVoteRepositoryPort postVoteRepositoryPort;
    private final CommentVoteRepositoryPort commentVoteRepositoryPort;
    private final GetMemberNicknamePort getMemberNicknamePort;
    private final GetMemberTierPort getMemberTierPort;
    private final GetRecordSummaryPort getRecordSummaryPort;
    private final GetPostAttachmentsPort getPostAttachmentsPort;

    public List<BoardResponse> listBoards(String lang) {
        Language language = Language.fromCode(lang);
        return boardRepositoryPort.findAllByOrderByDisplayOrderAsc()
                .stream().map(board -> BoardResponse.from(board, language)).toList();
    }

    public Page<PostSummaryResponse> listPosts(Long boardSno, int page, int size, String sort, String lang) {
        Language language = Language.fromCode(lang);

        boardRepositoryPort.findById(boardSno)
                .orElseThrow(() -> new IllegalArgumentException("board not found"));

        int pageSize = (size <= 0) ? 20 : size;
        Sort sortOrder = "top".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "score").and(Sort.by(Sort.Direction.DESC, "createAt"))
                : Sort.by(Sort.Direction.DESC, "createAt");

        var pageable = PageRequest.of(Math.max(page, 0), pageSize, sortOrder);

        return postRepositoryPort.findByBoardSno(boardSno, pageable)
                .map(post -> PostSummaryResponse.from(
                        post, nicknameOf(post.getMemberSno()), tierOf(post.getMemberSno()), language,
                        getPostAttachmentsPort.countByPostSno(post.getSno()),
                        commentRepositoryPort.countByPostSno(post.getSno())
                ));
    }

    @Transactional
    public PostDetailResponse getPost(Long memberSno, Long postSno, String lang) {
        Language language = Language.fromCode(lang);

        Post post = postRepositoryPort.findById(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        postRepositoryPort.incrementViewCount(postSno);

        Board board = boardRepositoryPort.findById(post.getBoardSno())
                .orElseThrow(() -> new IllegalArgumentException("board not found"));

        Integer myVote = myVoteOnPost(memberSno, postSno);
        RunningRecordResponse attachedRecord = post.getRecordSno() == null
                ? null
                : getRecordSummaryPort.getSummary(post.getRecordSno()).orElse(null);
        RankTier authorTier = tierOf(post.getMemberSno());
        String boardName = BoardLabelPolicy.name(board.getSlug(), language, board.getName());
        List<MediaAttachmentResponse> attachments = getPostAttachmentsPort.getByPostSno(post.getSno());

        // post.getViewCount()는 위 incrementViewCount()가 반영되기 전 값이므로 +1 해서 응답한다.
        return new PostDetailResponse(
                post.getSno(), post.getBoardSno(), boardName, post.getTitle(), post.getContent(),
                post.getMemberSno(), nicknameOf(post.getMemberSno()), authorTier, RankTierLabelPolicy.label(authorTier, language), attachedRecord,
                attachments,
                post.getViewCount() + 1, post.getScore(), myVote,
                post.getCreateAt(), post.getUpdateAt()
        );
    }

    public List<CommentResponse> listComments(Long memberSno, Long postSno) {
        if (!postRepositoryPort.existsById(postSno)) {
            throw new IllegalArgumentException("post not found");
        }

        List<Comment> all = commentRepositoryPort.findByPostSnoOrderByCreateAtAsc(postSno);

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
        Integer myVote = myVoteOnComment(memberSno, comment.getSno());
        return CommentResponse.from(comment, nicknameOf(comment.getMemberSno()), myVote, replies);
    }

    private Integer myVoteOnPost(Long memberSno, Long postSno) {
        if (memberSno == null) {
            return null;
        }
        return postVoteRepositoryPort.findByPostSnoAndMemberSno(postSno, memberSno)
                .map(vote -> vote.getVoteValue())
                .orElse(null);
    }

    private Integer myVoteOnComment(Long memberSno, Long commentSno) {
        if (memberSno == null) {
            return null;
        }
        return commentVoteRepositoryPort.findByCommentSnoAndMemberSno(commentSno, memberSno)
                .map(vote -> vote.getVoteValue())
                .orElse(null);
    }

    private String nicknameOf(Long memberSno) {
        return getMemberNicknamePort.getNickname(memberSno);
    }

    private RankTier tierOf(Long memberSno) {
        return getMemberTierPort.getTier(memberSno);
    }
}

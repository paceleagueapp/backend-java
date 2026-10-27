package com.paceleague.board.application.service;

import com.paceleague.board.application.dto.BoardResponse;
import com.paceleague.board.application.dto.CommentResponse;
import com.paceleague.board.application.dto.PostDetailResponse;
import com.paceleague.board.application.dto.PostSummaryResponse;
import com.paceleague.board.application.port.in.BoardQueryUseCase;
import com.paceleague.board.application.port.out.BoardRepositoryPort;
import com.paceleague.board.application.port.out.CommentRepositoryPort;
import com.paceleague.board.application.port.out.CommentVoteRepositoryPort;
import com.paceleague.board.application.port.out.PostRepositoryPort;
import com.paceleague.board.application.port.out.PostVoteRepositoryPort;
import com.paceleague.board.domain.entity.Board;
import com.paceleague.board.domain.entity.Comment;
import com.paceleague.board.domain.entity.Post;
import com.paceleague.board.domain.policy.BoardLabelPolicy;
import com.paceleague.common.i18n.Language;
import com.paceleague.crew.application.port.in.shared.GetMemberCrewBadgePort;
import com.paceleague.crew.application.port.in.shared.GetMemberCrewBadgePort.CrewBadge;
import com.paceleague.media.application.dto.MediaAttachmentResponse;
import com.paceleague.media.application.port.in.shared.GetPostAttachmentsPort;
import com.paceleague.member.application.port.in.shared.GetBlockedMemberSnosPort;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.rank.application.port.in.shared.GetMemberTierPort;
import com.paceleague.rank.domain.enums.RankTier;
import com.paceleague.rank.domain.policy.RankTierLabelPolicy;
import com.paceleague.record.application.dto.RunningRecordResponse;
import com.paceleague.record.application.dto.CoursePoint;
import com.paceleague.record.application.port.in.shared.GetRecordCoursePort;
import com.paceleague.record.application.port.in.shared.GetRecordSummaryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardQueryService implements BoardQueryUseCase {

    private final BoardRepositoryPort boardRepositoryPort;
    private final PostRepositoryPort postRepositoryPort;
    private final CommentRepositoryPort commentRepositoryPort;
    private final PostVoteRepositoryPort postVoteRepositoryPort;
    private final CommentVoteRepositoryPort commentVoteRepositoryPort;
    private final GetMemberNicknamePort getMemberNicknamePort;
    private final GetMemberTierPort getMemberTierPort;
    private final GetRecordSummaryPort getRecordSummaryPort;
    private final GetPostAttachmentsPort getPostAttachmentsPort;
    private final GetMemberCrewBadgePort getMemberCrewBadgePort;
    private final GetBlockedMemberSnosPort getBlockedMemberSnosPort;
    private final GetRecordCoursePort getRecordCoursePort;

    static final String COURSE_BOARD_SLUG = "course";

    // 목록 정렬 — new(날짜순, 기본) / likes(좋아요순, 예전 이름 top 도 허용) / comments(댓글 많은순).
    enum PostSort {
        NEW, LIKES, COMMENTS;

        static PostSort from(String sort) {
            if (sort == null) return NEW;
            return switch (sort.toLowerCase()) {
                case "likes", "top" -> LIKES;
                case "comments" -> COMMENTS;
                default -> NEW;
            };
        }
    }

    public List<BoardResponse> listBoards(String lang) {
        Language language = Language.fromCode(lang);
        return boardRepositoryPort.findAllByOrderByDisplayOrderAsc()
                .stream().map(board -> BoardResponse.from(board, language)).toList();
    }

    public Page<PostSummaryResponse> listPosts(Long viewerMemberSno, Long boardSno, int page, int size, String sort, String lang) {
        Language language = Language.fromCode(lang);

        boardRepositoryPort.findById(boardSno)
                .orElseThrow(() -> new IllegalArgumentException("board not found"));

        int pageSize = (size <= 0) ? 20 : size;
        int pageIndex = Math.max(page, 0);

        // 숨김(신고 누적) 글 + 내가 차단한 작성자의 글을 제외. JPQL `not in ()` 이 빈 컬렉션을 못 받아 sentinel(-1) 을 넣는다.
        Set<Long> blocked = getBlockedMemberSnosPort.getBlockedBy(viewerMemberSno);
        Collection<Long> blockedParam = blocked.isEmpty() ? List.of(-1L) : blocked;
        Page<Post> posts = switch (PostSort.from(sort)) {
            case LIKES -> postRepositoryPort.findVisibleByBoardSno(boardSno, blockedParam, PageRequest.of(pageIndex, pageSize,
                    Sort.by(Sort.Direction.DESC, "score").and(Sort.by(Sort.Direction.DESC, "createAt"))));
            case COMMENTS -> postRepositoryPort.findVisibleByBoardSnoOrderByCommentCount(boardSno, blockedParam,
                    PageRequest.of(pageIndex, pageSize));
            case NEW -> postRepositoryPort.findVisibleByBoardSno(boardSno, blockedParam, PageRequest.of(pageIndex, pageSize,
                    Sort.by(Sort.Direction.DESC, "createAt")));
        };
        Map<Long, CrewBadge> crewBadges = getMemberCrewBadgePort.getBadges(
                posts.stream().map(Post::getMemberSno).distinct().toList());

        return posts.map(post -> PostSummaryResponse.from(
                post, nicknameOf(post.getMemberSno()), tierOf(post.getMemberSno()),
                crewBadges.get(post.getMemberSno()), language,
                commentRepositoryPort.countByPostSno(post.getSno())
        ));
    }

    @Transactional
    public PostDetailResponse getPost(Long memberSno, Long postSno, String lang) {
        Language language = Language.fromCode(lang);

        Post post = postRepositoryPort.findById(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        if (post.isHidden()) {
            throw new IllegalArgumentException("This post has been deleted or hidden.");
        }

        postRepositoryPort.incrementViewCount(postSno);

        Board board = boardRepositoryPort.findById(post.getBoardSno())
                .orElseThrow(() -> new IllegalArgumentException("board not found"));

        Integer myVote = myVoteOnPost(memberSno, postSno);
        RunningRecordResponse attachedRecord = post.getRecordSno() == null
                ? null
                : getRecordSummaryPort.getSummary(post.getRecordSno()).orElse(null);
        // 경로(지도)는 코스추천 게시판에서만 공개 — 다른 게시판(인증 등)의 첨부 러닝은 지금처럼 요약만 보인다.
        List<CoursePoint> course = attachedRecord != null && COURSE_BOARD_SLUG.equals(board.getSlug())
                ? getRecordCoursePort.getCourse(post.getRecordSno())
                : List.of();
        RankTier authorTier = tierOf(post.getMemberSno());
        CrewBadge authorCrew = getMemberCrewBadgePort.getBadge(post.getMemberSno()).orElse(null);
        String boardName = BoardLabelPolicy.name(board.getSlug(), language, board.getName());
        List<MediaAttachmentResponse> attachments = getPostAttachmentsPort.getByPostSno(post.getSno());

        // post.getViewCount()는 위 incrementViewCount()가 반영되기 전 값이므로 +1 해서 응답한다.
        return new PostDetailResponse(
                post.getSno(), post.getBoardSno(), boardName, post.getTitle(), post.getContent(),
                post.getMemberSno(), nicknameOf(post.getMemberSno()), authorTier, RankTierLabelPolicy.label(authorTier, language),
                authorCrew == null ? null : authorCrew.crewName(),
                authorCrew == null ? null : authorCrew.crewIconUrl(),
                attachedRecord,
                course,
                attachments,
                post.getViewCount() + 1, post.getScore(), myVote,
                post.getCreateAt(), post.getUpdateAt()
        );
    }

    public List<CommentResponse> listComments(Long memberSno, Long postSno) {
        if (!postRepositoryPort.existsById(postSno)) {
            throw new IllegalArgumentException("post not found");
        }

        // 숨김(신고 누적) 댓글 + 내가 차단한 작성자의 댓글은 제외한다(부모가 빠지면 그 대댓글도 함께 사라짐 — MVP).
        Set<Long> blocked = getBlockedMemberSnosPort.getBlockedBy(memberSno);
        List<Comment> all = commentRepositoryPort.findByPostSnoOrderByCreateAtAsc(postSno).stream()
                .filter(c -> !c.isHidden() && !blocked.contains(c.getMemberSno()))
                .toList();

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

package com.example.paceleague.board.application.service;

import com.example.paceleague.board.application.dto.CommentCreateRequest;
import com.example.paceleague.board.application.dto.PostCreateRequest;
import com.example.paceleague.board.application.dto.VoteResponse;
import com.example.paceleague.board.application.port.in.BoardService;
import com.example.paceleague.board.application.port.out.BoardRepositoryPort;
import com.example.paceleague.board.application.port.out.CommentRepositoryPort;
import com.example.paceleague.board.application.port.out.CommentVoteRepositoryPort;
import com.example.paceleague.board.application.port.out.PostRepositoryPort;
import com.example.paceleague.board.application.port.out.PostVoteRepositoryPort;
import com.example.paceleague.board.domain.entity.Comment;
import com.example.paceleague.board.domain.entity.CommentVote;
import com.example.paceleague.board.domain.entity.Post;
import com.example.paceleague.board.domain.entity.PostVote;
import com.example.paceleague.board.domain.enums.VoteType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private static final int TITLE_MAX_LENGTH = 200;
    private static final int COMMENT_MAX_LENGTH = 1000;
    // 번역 API 비용이 글자 수에 비례하므로 게시글 본문 길이를 제한한다(기존엔 무제한이었음).
    private static final int CONTENT_MAX_LENGTH = 10_000;

    private final BoardRepositoryPort boardRepositoryPort;
    private final PostRepositoryPort postRepositoryPort;
    private final CommentRepositoryPort commentRepositoryPort;
    private final PostVoteRepositoryPort postVoteRepositoryPort;
    private final CommentVoteRepositoryPort commentVoteRepositoryPort;

    @Transactional
    public Long createPost(Long memberSno, Long boardSno, PostCreateRequest req) {
        requireNonBlank(req.title(), "title", TITLE_MAX_LENGTH);
        requireNonBlank(req.content(), "content", CONTENT_MAX_LENGTH);

        boardRepositoryPort.findById(boardSno)
                .orElseThrow(() -> new IllegalArgumentException("board not found"));

        Post post = Post.create(boardSno, memberSno, req.title(), req.content());
        postRepositoryPort.save(post);

        return post.getSno();
    }

    @Transactional
    public void deletePost(Long memberSno, Long postSno) {
        Post post = postRepositoryPort.findBySnoAndMemberSno(postSno, memberSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        List<Long> commentSnos = commentRepositoryPort.findByPostSnoOrderByCreateAtAsc(postSno)
                .stream().map(Comment::getSno).toList();

        if (!commentSnos.isEmpty()) {
            commentVoteRepositoryPort.deleteByCommentSnoIn(commentSnos);
        }
        commentRepositoryPort.deleteByPostSno(postSno);
        postVoteRepositoryPort.deleteByPostSno(postSno);
        postRepositoryPort.delete(post);
    }

    @Transactional
    public VoteResponse votePost(Long memberSno, Long postSno, int voteValue) {
        VoteType voteType = VoteType.fromValue(voteValue);

        Post post = postRepositoryPort.findBySnoForUpdate(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        Optional<PostVote> existing = postVoteRepositoryPort.findByPostSnoAndMemberSno(postSno, memberSno);
        Integer myVote = applyPostVote(post, memberSno, existing, voteType);

        postRepositoryPort.save(post);
        return new VoteResponse(post.getScore(), myVote);
    }

    private Integer applyPostVote(Post post, Long memberSno, Optional<PostVote> existing, VoteType voteType) {
        if (existing.isEmpty()) {
            postVoteRepositoryPort.save(PostVote.create(post.getSno(), memberSno, voteType));
            post.applyVoteDelta(voteType.getValue());
            return voteType.getValue();
        }

        PostVote vote = existing.get();
        if (vote.getVoteValue() == voteType.getValue()) {
            postVoteRepositoryPort.delete(vote);
            post.applyVoteDelta(-voteType.getValue());
            return null;
        }

        int oldValue = vote.getVoteValue();
        vote.changeValue(voteType);
        postVoteRepositoryPort.save(vote);
        post.applyVoteDelta(voteType.getValue() - oldValue);
        return voteType.getValue();
    }

    @Transactional
    public Long createComment(Long memberSno, Long postSno, CommentCreateRequest req) {
        requireNonBlank(req.content(), "content", COMMENT_MAX_LENGTH);

        postRepositoryPort.findById(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        Long parentCommentSno = req.parentCommentSno();
        if (parentCommentSno != null) {
            Comment parent = commentRepositoryPort.findById(parentCommentSno)
                    .orElseThrow(() -> new IllegalArgumentException("parent comment not found"));
            if (!parent.getPostSno().equals(postSno)) {
                throw new IllegalArgumentException("parent comment not found");
            }
            if (parent.isReply()) {
                throw new IllegalArgumentException("cannot reply to a reply");
            }
        }

        Comment comment = Comment.create(postSno, memberSno, parentCommentSno, req.content());
        commentRepositoryPort.save(comment);

        return comment.getSno();
    }

    @Transactional
    public void deleteComment(Long memberSno, Long commentSno) {
        Comment comment = commentRepositoryPort.findBySnoAndMemberSno(commentSno, memberSno)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        List<Long> replySnos = commentRepositoryPort.findByParentCommentSno(commentSno)
                .stream().map(Comment::getSno).toList();

        List<Long> allSnos = new ArrayList<>(replySnos);
        allSnos.add(commentSno);
        commentVoteRepositoryPort.deleteByCommentSnoIn(allSnos);

        commentRepositoryPort.deleteByParentCommentSno(commentSno);
        commentRepositoryPort.delete(comment);
    }

    @Transactional
    public VoteResponse voteComment(Long memberSno, Long commentSno, int voteValue) {
        VoteType voteType = VoteType.fromValue(voteValue);

        Comment comment = commentRepositoryPort.findBySnoForUpdate(commentSno)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        Optional<CommentVote> existing = commentVoteRepositoryPort.findByCommentSnoAndMemberSno(commentSno, memberSno);
        Integer myVote = applyCommentVote(comment, memberSno, existing, voteType);

        commentRepositoryPort.save(comment);
        return new VoteResponse(comment.getScore(), myVote);
    }

    private Integer applyCommentVote(Comment comment, Long memberSno, Optional<CommentVote> existing, VoteType voteType) {
        if (existing.isEmpty()) {
            commentVoteRepositoryPort.save(CommentVote.create(comment.getSno(), memberSno, voteType));
            comment.applyVoteDelta(voteType.getValue());
            return voteType.getValue();
        }

        CommentVote vote = existing.get();
        if (vote.getVoteValue() == voteType.getValue()) {
            commentVoteRepositoryPort.delete(vote);
            comment.applyVoteDelta(-voteType.getValue());
            return null;
        }

        int oldValue = vote.getVoteValue();
        vote.changeValue(voteType);
        commentVoteRepositoryPort.save(vote);
        comment.applyVoteDelta(voteType.getValue() - oldValue);
        return voteType.getValue();
    }

    private void requireNonBlank(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " is too long (max " + maxLength + ")");
        }
    }
}

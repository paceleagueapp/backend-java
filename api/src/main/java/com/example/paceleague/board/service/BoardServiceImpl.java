package com.example.paceleague.board.service;

import com.example.paceleague.board.dto.CommentCreateRequest;
import com.example.paceleague.board.dto.PostCreateRequest;
import com.example.paceleague.board.dto.VoteResponse;
import com.example.paceleague.board.entity.Comment;
import com.example.paceleague.board.entity.CommentVote;
import com.example.paceleague.board.entity.Post;
import com.example.paceleague.board.entity.PostVote;
import com.example.paceleague.board.enums.VoteType;
import com.example.paceleague.board.repository.BoardRepository;
import com.example.paceleague.board.repository.CommentRepository;
import com.example.paceleague.board.repository.CommentVoteRepository;
import com.example.paceleague.board.repository.PostRepository;
import com.example.paceleague.board.repository.PostVoteRepository;
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

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;

    @Transactional
    public Long createPost(Long memberSno, Long boardSno, PostCreateRequest req) {
        requireNonBlank(req.title(), "title", TITLE_MAX_LENGTH);
        requireNonBlank(req.content(), "content", CONTENT_MAX_LENGTH);

        boardRepository.findById(boardSno)
                .orElseThrow(() -> new IllegalArgumentException("board not found"));

        Post post = Post.create(boardSno, memberSno, req.title(), req.content());
        postRepository.save(post);

        return post.getSno();
    }

    @Transactional
    public void deletePost(Long memberSno, Long postSno) {
        Post post = postRepository.findBySnoAndMemberSno(postSno, memberSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        List<Long> commentSnos = commentRepository.findByPostSnoOrderByCreateAtAsc(postSno)
                .stream().map(Comment::getSno).toList();

        if (!commentSnos.isEmpty()) {
            commentVoteRepository.deleteByCommentSnoIn(commentSnos);
        }
        commentRepository.deleteByPostSno(postSno);
        postVoteRepository.deleteByPostSno(postSno);
        postRepository.delete(post);
    }

    @Transactional
    public VoteResponse votePost(Long memberSno, Long postSno, int voteValue) {
        VoteType voteType = VoteType.fromValue(voteValue);

        Post post = postRepository.findBySnoForUpdate(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        Optional<PostVote> existing = postVoteRepository.findByPostSnoAndMemberSno(postSno, memberSno);
        Integer myVote = applyPostVote(post, existing, voteType);

        postRepository.save(post);
        return new VoteResponse(post.getScore(), myVote);
    }

    private Integer applyPostVote(Post post, Optional<PostVote> existing, VoteType voteType) {
        if (existing.isEmpty()) {
            postVoteRepository.save(PostVote.create(post.getSno(), post.getMemberSno(), voteType));
            post.applyVoteDelta(voteType.getValue());
            return voteType.getValue();
        }

        PostVote vote = existing.get();
        if (vote.getVoteValue() == voteType.getValue()) {
            postVoteRepository.delete(vote);
            post.applyVoteDelta(-voteType.getValue());
            return null;
        }

        int oldValue = vote.getVoteValue();
        vote.changeValue(voteType);
        postVoteRepository.save(vote);
        post.applyVoteDelta(voteType.getValue() - oldValue);
        return voteType.getValue();
    }

    @Transactional
    public Long createComment(Long memberSno, Long postSno, CommentCreateRequest req) {
        requireNonBlank(req.content(), "content", COMMENT_MAX_LENGTH);

        postRepository.findById(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        Long parentCommentSno = req.parentCommentSno();
        if (parentCommentSno != null) {
            Comment parent = commentRepository.findById(parentCommentSno)
                    .orElseThrow(() -> new IllegalArgumentException("parent comment not found"));
            if (!parent.getPostSno().equals(postSno)) {
                throw new IllegalArgumentException("parent comment not found");
            }
            if (parent.isReply()) {
                throw new IllegalArgumentException("cannot reply to a reply");
            }
        }

        Comment comment = Comment.create(postSno, memberSno, parentCommentSno, req.content());
        commentRepository.save(comment);

        return comment.getSno();
    }

    @Transactional
    public void deleteComment(Long memberSno, Long commentSno) {
        Comment comment = commentRepository.findBySnoAndMemberSno(commentSno, memberSno)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        List<Long> replySnos = commentRepository.findByParentCommentSno(commentSno)
                .stream().map(Comment::getSno).toList();

        List<Long> allSnos = new ArrayList<>(replySnos);
        allSnos.add(commentSno);
        commentVoteRepository.deleteByCommentSnoIn(allSnos);

        commentRepository.deleteByParentCommentSno(commentSno);
        commentRepository.delete(comment);
    }

    @Transactional
    public VoteResponse voteComment(Long memberSno, Long commentSno, int voteValue) {
        VoteType voteType = VoteType.fromValue(voteValue);

        Comment comment = commentRepository.findBySnoForUpdate(commentSno)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        Optional<CommentVote> existing = commentVoteRepository.findByCommentSnoAndMemberSno(commentSno, memberSno);
        Integer myVote = applyCommentVote(comment, memberSno, existing, voteType);

        commentRepository.save(comment);
        return new VoteResponse(comment.getScore(), myVote);
    }

    private Integer applyCommentVote(Comment comment, Long memberSno, Optional<CommentVote> existing, VoteType voteType) {
        if (existing.isEmpty()) {
            commentVoteRepository.save(CommentVote.create(comment.getSno(), memberSno, voteType));
            comment.applyVoteDelta(voteType.getValue());
            return voteType.getValue();
        }

        CommentVote vote = existing.get();
        if (vote.getVoteValue() == voteType.getValue()) {
            commentVoteRepository.delete(vote);
            comment.applyVoteDelta(-voteType.getValue());
            return null;
        }

        int oldValue = vote.getVoteValue();
        vote.changeValue(voteType);
        commentVoteRepository.save(vote);
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

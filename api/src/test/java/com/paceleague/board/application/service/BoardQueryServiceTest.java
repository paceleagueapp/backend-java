package com.paceleague.board.application.service;

import com.paceleague.board.application.dto.PostDetailResponse;
import com.paceleague.board.application.port.out.BoardRepositoryPort;
import com.paceleague.board.application.port.out.CommentRepositoryPort;
import com.paceleague.board.application.port.out.CommentVoteRepositoryPort;
import com.paceleague.board.application.port.out.PostRepositoryPort;
import com.paceleague.board.application.port.out.PostVoteRepositoryPort;
import com.paceleague.board.domain.entity.Board;
import com.paceleague.board.domain.entity.Post;
import com.paceleague.crew.application.port.in.shared.GetMemberCrewBadgePort;
import com.paceleague.media.application.port.in.shared.GetPostAttachmentsPort;
import com.paceleague.member.application.port.in.shared.GetBlockedMemberSnosPort;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.rank.application.port.in.shared.GetMemberTierPort;
import com.paceleague.record.application.dto.CoursePoint;
import com.paceleague.record.application.dto.RunningRecordResponse;
import com.paceleague.record.application.port.in.shared.GetRecordCoursePort;
import com.paceleague.record.application.port.in.shared.GetRecordSummaryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoardQueryServiceTest {

    @Mock BoardRepositoryPort boardRepositoryPort;
    @Mock PostRepositoryPort postRepositoryPort;
    @Mock CommentRepositoryPort commentRepositoryPort;
    @Mock PostVoteRepositoryPort postVoteRepositoryPort;
    @Mock CommentVoteRepositoryPort commentVoteRepositoryPort;
    @Mock GetMemberNicknamePort getMemberNicknamePort;
    @Mock GetMemberTierPort getMemberTierPort;
    @Mock GetRecordSummaryPort getRecordSummaryPort;
    @Mock GetPostAttachmentsPort getPostAttachmentsPort;
    @Mock GetMemberCrewBadgePort getMemberCrewBadgePort;
    @Mock GetBlockedMemberSnosPort getBlockedMemberSnosPort;
    @Mock GetRecordCoursePort getRecordCoursePort;
    @InjectMocks BoardQueryService service;

    private Board board(String slug) {
        Board b = Board.create(slug, slug, null, 1);
        ReflectionTestUtils.setField(b, "sno", 1L);
        return b;
    }

    @BeforeEach
    void setUp() {
        when(boardRepositoryPort.findById(1L)).thenReturn(Optional.of(board("free")));
        when(getBlockedMemberSnosPort.getBlockedBy(any())).thenReturn(Set.of());
        when(getMemberCrewBadgePort.getBadges(any())).thenReturn(Map.of());
        when(getMemberTierPort.getTier(any())).thenReturn(com.paceleague.rank.domain.enums.RankTier.SILVER);
        when(postRepositoryPort.findVisibleByBoardSno(anyLong(), any(), any())).thenReturn(Page.empty());
        when(postRepositoryPort.findVisibleByBoardSnoOrderByCommentCount(anyLong(), any(), any())).thenReturn(Page.empty());
    }

    @Test
    void 기본_정렬은_날짜순() {
        service.listPosts(null, 1L, 0, 20, null, "ko");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepositoryPort).findVisibleByBoardSno(eq(1L), any(), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("createAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageable.getValue().getSort().getOrderFor("score")).isNull();
    }

    @Test
    void likes와_예전_이름_top은_좋아요순() {
        for (String sort : new String[] {"likes", "top"}) {
            org.mockito.Mockito.clearInvocations(postRepositoryPort);
            service.listPosts(null, 1L, 0, 20, sort, "ko");

            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            verify(postRepositoryPort).findVisibleByBoardSno(eq(1L), any(), pageable.capture());
            assertThat(pageable.getValue().getSort().toList().get(0).getProperty()).isEqualTo("score");
        }
    }

    @Test
    void comments는_댓글_많은순_쿼리를_쓴다() {
        service.listPosts(null, 1L, 0, 20, "comments", "ko");

        verify(postRepositoryPort).findVisibleByBoardSnoOrderByCommentCount(eq(1L), any(), any());
        verify(postRepositoryPort, never()).findVisibleByBoardSno(anyLong(), any(), any());
    }

    private Post postWithRecord(Long recordSno) {
        Post post = Post.create(1L, 7L, recordSno, "title", "<p>body</p>");
        ReflectionTestUtils.setField(post, "sno", 100L);
        when(postRepositoryPort.findById(100L)).thenReturn(Optional.of(post));
        when(getRecordSummaryPort.getSummary(recordSno)).thenReturn(Optional.of(RunningRecordResponse.builder().recordSno(recordSno).build()));
        when(getRecordCoursePort.getCourse(recordSno)).thenReturn(List.of(new CoursePoint(37.5, 127.0)));
        return post;
    }

    @Test
    void 코스추천_게시판_글은_첨부_러닝의_경로를_내려준다() {
        when(boardRepositoryPort.findById(1L)).thenReturn(Optional.of(board("course")));
        postWithRecord(55L);

        PostDetailResponse detail = service.getPost(null, 100L, "ko");

        assertThat(detail.course()).containsExactly(new CoursePoint(37.5, 127.0));
    }

    @Test
    void 다른_게시판은_첨부_러닝이_있어도_경로를_공개하지_않는다() {
        when(boardRepositoryPort.findById(1L)).thenReturn(Optional.of(board("verify")));
        postWithRecord(55L);

        PostDetailResponse detail = service.getPost(null, 100L, "ko");

        assertThat(detail.course()).isEmpty();
        verify(getRecordCoursePort, never()).getCourse(any());
    }
}

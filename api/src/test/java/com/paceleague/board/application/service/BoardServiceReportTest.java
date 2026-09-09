package com.paceleague.board.application.service;

import com.paceleague.board.application.port.out.BoardReportRepositoryPort;
import com.paceleague.board.application.port.out.BoardRepositoryPort;
import com.paceleague.board.application.port.out.CommentRepositoryPort;
import com.paceleague.board.application.port.out.CommentVoteRepositoryPort;
import com.paceleague.board.application.port.out.PostRepositoryPort;
import com.paceleague.board.application.port.out.PostVoteRepositoryPort;
import com.paceleague.board.domain.entity.Post;
import com.paceleague.board.domain.enums.ReportTargetType;
import com.paceleague.media.application.port.in.MediaUseCase;
import com.paceleague.record.application.port.in.RecordQueryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoardServiceReportTest {

    @Mock BoardRepositoryPort boardRepositoryPort;
    @Mock PostRepositoryPort postRepositoryPort;
    @Mock CommentRepositoryPort commentRepositoryPort;
    @Mock PostVoteRepositoryPort postVoteRepositoryPort;
    @Mock CommentVoteRepositoryPort commentVoteRepositoryPort;
    @Mock BoardReportRepositoryPort boardReportRepositoryPort;
    @Mock RecordQueryUseCase recordQueryUseCase;
    @Mock MediaUseCase mediaUseCase;

    BoardService service;
    Post post;

    @BeforeEach
    void setUp() {
        service = new BoardService(boardRepositoryPort, postRepositoryPort, commentRepositoryPort,
                postVoteRepositoryPort, commentVoteRepositoryPort, boardReportRepositoryPort,
                recordQueryUseCase, mediaUseCase);
        ReflectionTestUtils.setField(service, "reportAutoHideThreshold", 3);

        post = Post.create(1L, 100L, null, "제목", "<p>본문</p>");
        ReflectionTestUtils.setField(post, "sno", 5L);
        when(postRepositoryPort.findBySnoForUpdate(5L)).thenReturn(Optional.of(post));
    }

    @Test
    void 본인_게시글은_신고할_수_없다() {
        assertThatThrownBy(() -> service.reportPost(100L, 5L, "SPAM", null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(boardReportRepositoryPort, never()).save(any());
    }

    @Test
    void 유효하지_않은_사유는_400() {
        when(boardReportRepositoryPort.exists(9L, ReportTargetType.POST, 5L)).thenReturn(false);
        assertThatThrownBy(() -> service.reportPost(9L, 5L, "WHATEVER", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미_신고했으면_멱등하게_저장하지_않는다() {
        when(boardReportRepositoryPort.exists(9L, ReportTargetType.POST, 5L)).thenReturn(true);
        when(boardReportRepositoryPort.countDistinctReporters(ReportTargetType.POST, 5L)).thenReturn(1L);

        service.reportPost(9L, 5L, "SPAM", null);

        verify(boardReportRepositoryPort, never()).save(any());
        assertThat(post.isHidden()).isFalse();
    }

    @Test
    void 임계값_미만이면_저장만_하고_숨기지_않는다() {
        when(boardReportRepositoryPort.exists(9L, ReportTargetType.POST, 5L)).thenReturn(false);
        when(boardReportRepositoryPort.countDistinctReporters(ReportTargetType.POST, 5L)).thenReturn(2L);

        service.reportPost(9L, 5L, "ABUSE", "욕설");

        verify(boardReportRepositoryPort).save(any());
        assertThat(post.isHidden()).isFalse();
        verify(postRepositoryPort, never()).save(any());
    }

    @Test
    void 서로_다른_신고자_임계값_도달시_자동_숨김된다() {
        when(boardReportRepositoryPort.exists(9L, ReportTargetType.POST, 5L)).thenReturn(false);
        when(boardReportRepositoryPort.countDistinctReporters(ReportTargetType.POST, 5L)).thenReturn(3L);

        service.reportPost(9L, 5L, "SEXUAL", null);

        assertThat(post.isHidden()).isTrue();
        assertThat(post.getHiddenAt()).isNotNull();
        verify(postRepositoryPort).save(post);
    }
}

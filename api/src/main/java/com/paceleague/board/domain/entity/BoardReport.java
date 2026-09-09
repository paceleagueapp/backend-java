package com.paceleague.board.domain.entity;

import com.paceleague.board.domain.enums.ReportReason;
import com.paceleague.board.domain.enums.ReportTargetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 게시글/댓글 신고 1건. (reporter, target_type, target_sno) UNIQUE — 1인 1신고.
//  - OPEN: 처리 대기. 서로 다른 신고자 수가 임계값에 도달하면 대상이 자동 숨김된다.
//  - RESOLVED / DISMISSED: 운영자가 DB에서 수동 처리.
@Entity
@Table(name = "board_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardReport {

    public static final String STATUS_OPEN = "OPEN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "reporter_member_sno", nullable = false)
    private Long reporterMemberSno;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 10)
    private ReportTargetType targetType;

    @Column(name = "target_sno", nullable = false)
    private Long targetSno;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 20)
    private ReportReason reason;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private BoardReport(Long reporterMemberSno, ReportTargetType targetType, Long targetSno,
                        ReportReason reason, String detail) {
        this.reporterMemberSno = reporterMemberSno;
        this.targetType = targetType;
        this.targetSno = targetSno;
        this.reason = reason;
        this.detail = detail;
        this.status = STATUS_OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public static BoardReport create(Long reporterMemberSno, ReportTargetType targetType, Long targetSno,
                                     ReportReason reason, String detail) {
        return new BoardReport(reporterMemberSno, targetType, targetSno, reason, detail);
    }
}

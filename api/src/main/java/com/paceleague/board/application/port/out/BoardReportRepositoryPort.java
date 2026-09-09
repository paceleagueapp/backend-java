package com.paceleague.board.application.port.out;

import com.paceleague.board.domain.entity.BoardReport;
import com.paceleague.board.domain.enums.ReportTargetType;

public interface BoardReportRepositoryPort {
    BoardReport save(BoardReport report);

    boolean exists(Long reporterMemberSno, ReportTargetType targetType, Long targetSno);

    long countDistinctReporters(ReportTargetType targetType, Long targetSno);

    void deleteAllByReporter(Long memberSno);
}

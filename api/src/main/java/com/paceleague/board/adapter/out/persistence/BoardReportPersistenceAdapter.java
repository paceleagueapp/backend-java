package com.paceleague.board.adapter.out.persistence;

import com.paceleague.board.application.port.out.BoardReportRepositoryPort;
import com.paceleague.board.domain.entity.BoardReport;
import com.paceleague.board.domain.enums.ReportTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BoardReportPersistenceAdapter implements BoardReportRepositoryPort {

    private final BoardReportJpaRepository jpa;

    public BoardReport save(BoardReport report) {
        return jpa.save(report);
    }

    public boolean exists(Long reporterMemberSno, ReportTargetType targetType, Long targetSno) {
        return jpa.existsByReporterMemberSnoAndTargetTypeAndTargetSno(reporterMemberSno, targetType, targetSno);
    }

    public long countDistinctReporters(ReportTargetType targetType, Long targetSno) {
        return jpa.countDistinctReporters(targetType, targetSno);
    }

    @Transactional
    public void deleteAllByReporter(Long memberSno) {
        jpa.deleteAllByReporterMemberSno(memberSno);
    }
}

package com.paceleague.board.adapter.out.persistence;

import com.paceleague.board.domain.entity.BoardReport;
import com.paceleague.board.domain.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardReportJpaRepository extends JpaRepository<BoardReport, Long> {

    boolean existsByReporterMemberSnoAndTargetTypeAndTargetSno(
            Long reporterMemberSno, ReportTargetType targetType, Long targetSno);

    @Query("select count(distinct r.reporterMemberSno) from BoardReport r "
            + "where r.targetType = :type and r.targetSno = :sno and r.status = 'OPEN'")
    long countDistinctReporters(@Param("type") ReportTargetType type, @Param("sno") Long targetSno);

    @Modifying
    @Query("delete from BoardReport r where r.reporterMemberSno = :memberSno")
    int deleteAllByReporterMemberSno(@Param("memberSno") Long memberSno);
}

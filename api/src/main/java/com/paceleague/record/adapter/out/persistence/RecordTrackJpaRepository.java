package com.paceleague.record.adapter.out.persistence;

import com.paceleague.record.domain.entity.RecordTrack;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;

public interface RecordTrackJpaRepository extends JpaRepository<RecordTrack, Long> {
    Optional<RecordTrack> findByUnoAndClientRunId(Long uno, String clientRunId);

    Optional<RecordTrack> findByRecordSno(Long recordSno);

    @Query("select t.recordSno from RecordTrack t where t.uno = :uno and t.recordSno is not null")
    List<Long> findRecordSnosByUno(@Param("uno") Long uno);

    @Query("""
            select t.sno from RecordTrack t
            where t.status = 'ACTIVE' and t.updateAt < :idleBefore
            order by t.updateAt asc
            """)
    List<Long> findIdleActiveSessionSnos(@Param("idleBefore") LocalDateTime idleBefore, Pageable pageable);

    @Query("""
            select t.sno from RecordTrack t
            where t.territoryMode = true and t.status = 'FINISHED'
            order by t.endedAt asc
            """)
    List<Long> findFinishedTerritoryModeSnosOrderByEndedAt();

    // 회원 탈퇴 시 GPS 트랙 일괄 삭제 (points_json LONGTEXT 를 로드하지 않도록 벌크 delete).
    @Modifying
    @Query("delete from RecordTrack t where t.uno = :uno")
    int deleteAllByUno(@Param("uno") Long uno);
}

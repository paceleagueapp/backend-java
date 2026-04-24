package com.example.paceleague.rank.repository;

import com.example.paceleague.rank.entity.MemberScore;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberScoreRepository extends JpaRepository<MemberScore, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select ms
    from MemberScore ms
    where ms.memberSno = :memberSno
    and ms.seasonSno = :seasonSno
""")
    Optional<MemberScore> findByMemberSnoAndSeasonSnoForUpdate(
            @Param("memberSno") Long memberSno,
            @Param("seasonSno") Long seasonSno
    );

    Optional<MemberScore> findByMemberSnoAndSeasonSno(Long memberSno, Long seasonSno);
}

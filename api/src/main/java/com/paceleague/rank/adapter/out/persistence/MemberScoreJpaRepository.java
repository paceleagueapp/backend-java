package com.paceleague.rank.adapter.out.persistence;

import com.paceleague.rank.domain.entity.MemberScore;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;

public interface MemberScoreJpaRepository extends JpaRepository<MemberScore, Long> {

    List<MemberScore> findByMemberSnoInAndSeasonSno(Collection<Long> memberSnos, Long seasonSno);
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

    // 회원 탈퇴 시 시즌 누적 점수 삭제 (랭킹에서 제외).
    @Modifying
    @Query("delete from MemberScore ms where ms.memberSno = :memberSno")
    int deleteAllByMemberSno(@Param("memberSno") Long memberSno);
}

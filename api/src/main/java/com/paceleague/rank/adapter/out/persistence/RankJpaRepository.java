package com.paceleague.rank.adapter.out.persistence;

import com.paceleague.rank.domain.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankJpaRepository extends JpaRepository<Rank, Long> {

    // 회원 탈퇴 시 점수 로그 일괄 삭제.
    @Modifying
    @Query("delete from Rank r where r.uno = :uno")
    int deleteAllByUno(@Param("uno") Long uno);
}

package com.paceleague.member.adapter.out.persistence;

import com.paceleague.member.domain.entity.MemberBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberBlockJpaRepository extends JpaRepository<MemberBlock, Long> {

    boolean existsByBlockerMemberSnoAndBlockedMemberSno(Long blockerMemberSno, Long blockedMemberSno);

    void deleteByBlockerMemberSnoAndBlockedMemberSno(Long blockerMemberSno, Long blockedMemberSno);

    List<MemberBlock> findByBlockerMemberSnoOrderByCreatedAtDesc(Long blockerMemberSno);

    @Query("select b.blockedMemberSno from MemberBlock b where b.blockerMemberSno = :sno")
    List<Long> findBlockedSnos(@Param("sno") Long blockerMemberSno);

    @Modifying
    @Query("delete from MemberBlock b where b.blockerMemberSno = :sno or b.blockedMemberSno = :sno")
    int deleteAllByMember(@Param("sno") Long memberSno);
}

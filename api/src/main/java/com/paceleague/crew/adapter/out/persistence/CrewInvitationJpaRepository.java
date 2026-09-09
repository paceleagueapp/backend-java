package com.paceleague.crew.adapter.out.persistence;

import com.paceleague.crew.domain.entity.CrewInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewInvitationJpaRepository extends JpaRepository<CrewInvitation, Long> {

    List<CrewInvitation> findByInviteeMemberSnoAndStatusOrderByCreateAtDesc(Long inviteeMemberSno, String status);

    boolean existsByCrewSnoAndInviteeMemberSnoAndStatus(Long crewSno, Long inviteeMemberSno, String status);

    void deleteByCrewSno(Long crewSno);

    @Modifying
    @Query("delete from CrewInvitation i where i.inviterMemberSno = :m or i.inviteeMemberSno = :m")
    int deleteAllByMember(@Param("m") Long memberSno);
}

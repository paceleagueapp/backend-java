package com.example.paceleague.crew.adapter.out.persistence;

import com.example.paceleague.crew.domain.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CrewMemberJpaRepository extends JpaRepository<CrewMember, Long> {

    Optional<CrewMember> findByMemberSno(Long memberSno);

    Optional<CrewMember> findByCrewSnoAndMemberSno(Long crewSno, Long memberSno);

    List<CrewMember> findByCrewSnoOrderByJoinedAtAsc(Long crewSno);

    boolean existsByMemberSno(Long memberSno);

    void deleteByCrewSno(Long crewSno);

    @Query(value = """
            select cm.member_sno as memberSno, c.name as crewName, c.icon_url as crewIconUrl
            from crew_member cm
            join crew c on c.sno = cm.crew_sno
            where cm.member_sno in (:memberSnos)
            """, nativeQuery = true)
    List<CrewMemberBadgeProjection> findBadgesByMemberSnoIn(@Param("memberSnos") Collection<Long> memberSnos);
}

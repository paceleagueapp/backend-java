package com.paceleague.member.adapter.out.persistence;

import com.paceleague.member.domain.entity.AgreementType;
import com.paceleague.member.domain.entity.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberAgreementJpaRepository extends JpaRepository<MemberAgreement, Long> {
    List<MemberAgreement> findAllByMemberSno(Long memberSno);

    Optional<MemberAgreement> findByMemberSnoAndAgreementType(Long memberSno, AgreementType agreementType);

    @Query("select a.memberSno from MemberAgreement a"
            + " where a.agreementType = :type and a.agreed = false and a.memberSno in :memberSnos")
    List<Long> findDeclinedMemberSnos(@Param("type") AgreementType type, @Param("memberSnos") Collection<Long> memberSnos);
}

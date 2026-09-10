package com.paceleague.member.adapter.out.persistence;

import com.paceleague.member.domain.entity.AgreementType;
import com.paceleague.member.domain.entity.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberAgreementJpaRepository extends JpaRepository<MemberAgreement, Long> {
    List<MemberAgreement> findAllByMemberSno(Long memberSno);

    Optional<MemberAgreement> findByMemberSnoAndAgreementType(Long memberSno, AgreementType agreementType);
}

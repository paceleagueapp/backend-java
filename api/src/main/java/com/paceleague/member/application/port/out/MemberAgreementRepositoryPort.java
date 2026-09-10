package com.paceleague.member.application.port.out;

import com.paceleague.member.domain.entity.AgreementType;
import com.paceleague.member.domain.entity.MemberAgreement;

import java.util.List;
import java.util.Optional;

public interface MemberAgreementRepositoryPort {
    List<MemberAgreement> findAllByMemberSno(Long memberSno);

    Optional<MemberAgreement> findByMemberSnoAndAgreementType(Long memberSno, AgreementType agreementType);

    MemberAgreement save(MemberAgreement agreement);
}

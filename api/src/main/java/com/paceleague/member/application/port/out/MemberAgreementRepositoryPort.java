package com.paceleague.member.application.port.out;

import com.paceleague.member.domain.entity.AgreementType;
import com.paceleague.member.domain.entity.MemberAgreement;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberAgreementRepositoryPort {
    List<MemberAgreement> findAllByMemberSno(Long memberSno);

    Optional<MemberAgreement> findByMemberSnoAndAgreementType(Long memberSno, AgreementType agreementType);

    MemberAgreement save(MemberAgreement agreement);

    // 주어진 회원 중 해당 동의를 명시적으로 거부(agreed=false)한 회원의 sno.
    List<Long> findDeclinedMemberSnos(AgreementType agreementType, Collection<Long> memberSnos);
}

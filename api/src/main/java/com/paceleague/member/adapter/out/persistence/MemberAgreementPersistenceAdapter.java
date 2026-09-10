package com.paceleague.member.adapter.out.persistence;

import com.paceleague.member.application.port.out.MemberAgreementRepositoryPort;
import com.paceleague.member.domain.entity.AgreementType;
import com.paceleague.member.domain.entity.MemberAgreement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberAgreementPersistenceAdapter implements MemberAgreementRepositoryPort {

    private final MemberAgreementJpaRepository memberAgreementJpaRepository;

    public List<MemberAgreement> findAllByMemberSno(Long memberSno) {
        return memberAgreementJpaRepository.findAllByMemberSno(memberSno);
    }

    public Optional<MemberAgreement> findByMemberSnoAndAgreementType(Long memberSno, AgreementType agreementType) {
        return memberAgreementJpaRepository.findByMemberSnoAndAgreementType(memberSno, agreementType);
    }

    public MemberAgreement save(MemberAgreement agreement) {
        return memberAgreementJpaRepository.save(agreement);
    }
}

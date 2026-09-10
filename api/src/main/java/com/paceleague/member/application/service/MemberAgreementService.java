package com.paceleague.member.application.service;

import com.paceleague.member.application.dto.AgreementItem;
import com.paceleague.member.application.dto.AgreementStatusResponse;
import com.paceleague.member.application.port.in.MemberAgreementUseCase;
import com.paceleague.member.application.port.out.MemberAgreementRepositoryPort;
import com.paceleague.member.domain.entity.AgreementType;
import com.paceleague.member.domain.entity.MemberAgreement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAgreementService implements MemberAgreementUseCase {

    private final MemberAgreementRepositoryPort memberAgreementRepositoryPort;

    @Override
    public List<AgreementStatusResponse> getStatus(Long memberSno) {
        Map<AgreementType, MemberAgreement> existing = new EnumMap<>(AgreementType.class);
        for (MemberAgreement a : memberAgreementRepositoryPort.findAllByMemberSno(memberSno)) {
            existing.put(a.getAgreementType(), a);
        }

        return List.of(AgreementType.values()).stream()
                .map(type -> {
                    MemberAgreement a = existing.get(type);
                    return a == null
                            ? new AgreementStatusResponse(type, false, false, type.currentVersion())
                            : new AgreementStatusResponse(type, true, a.isAgreed(), a.getAgreementVersion());
                })
                .toList();
    }

    @Override
    @Transactional
    public void updateAgreements(Long memberSno, List<AgreementItem> items) {
        for (AgreementItem item : items) {
            MemberAgreement existing = memberAgreementRepositoryPort
                    .findByMemberSnoAndAgreementType(memberSno, item.agreementType())
                    .orElse(null);

            if (existing == null) {
                memberAgreementRepositoryPort.save(
                        MemberAgreement.create(memberSno, item.agreementType(), item.agreed()));
            } else {
                existing.update(item.agreed());
                memberAgreementRepositoryPort.save(existing);
            }
        }
    }
}

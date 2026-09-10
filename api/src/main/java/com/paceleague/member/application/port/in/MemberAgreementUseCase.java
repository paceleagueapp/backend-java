package com.paceleague.member.application.port.in;

import com.paceleague.member.application.dto.AgreementItem;
import com.paceleague.member.application.dto.AgreementStatusResponse;

import java.util.List;

public interface MemberAgreementUseCase {
    List<AgreementStatusResponse> getStatus(Long memberSno);

    void updateAgreements(Long memberSno, List<AgreementItem> items);
}

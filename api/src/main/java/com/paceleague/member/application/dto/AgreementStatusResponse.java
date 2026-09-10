package com.paceleague.member.application.dto;

import com.paceleague.member.domain.entity.AgreementType;

// answered=false면 아직 이 동의 항목에 대한 응답 자체가 없다는 뜻(로그인 시 동의 페이지로 보낼지 판단 기준).
public record AgreementStatusResponse(
        AgreementType agreementType,
        boolean answered,
        boolean agreed,
        String version
) {}

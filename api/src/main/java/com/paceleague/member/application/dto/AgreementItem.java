package com.paceleague.member.application.dto;

import com.paceleague.member.domain.entity.AgreementType;
import jakarta.validation.constraints.NotNull;

public record AgreementItem(
        @NotNull AgreementType agreementType,
        @NotNull Boolean agreed
) {}

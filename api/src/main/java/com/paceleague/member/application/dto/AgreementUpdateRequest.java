package com.paceleague.member.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AgreementUpdateRequest(
        @NotEmpty @Valid List<AgreementItem> agreements
) {}

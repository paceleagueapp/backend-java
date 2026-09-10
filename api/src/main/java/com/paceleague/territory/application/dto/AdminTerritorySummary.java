package com.paceleague.territory.application.dto;

import java.time.LocalDateTime;

public record AdminTerritorySummary(
        Long sno,
        Long ownerMemberSno,
        String ownerNickname,
        double areaSqm,
        Integer hexCount,
        Long season,
        LocalDateTime createdAt
) {}

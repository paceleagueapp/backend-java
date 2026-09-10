package com.paceleague.ranking.application.dto;

public record AdminRankingEntry(
        int rank,
        Long memberSno,
        String nickname,
        int totalScore,
        String tier
) {}

package com.paceleague.rank.application.dto;

public record ApplyScoreCommand(
        Long memberSno,
        Long seasonSno,
        int totalScore,
        int scaledScore,
        int addScore,
        String utcOffset
) {
}

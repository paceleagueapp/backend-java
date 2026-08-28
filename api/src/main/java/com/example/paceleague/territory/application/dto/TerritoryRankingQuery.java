package com.example.paceleague.territory.application.dto;

// 땅따먹기 면적 랭킹 질의. lang은 티어 라벨 언어, memberSno는 로그인 시 mine 플래그용(비로그인 null).
public record TerritoryRankingQuery(
        String lang,
        Long memberSno
) {
}

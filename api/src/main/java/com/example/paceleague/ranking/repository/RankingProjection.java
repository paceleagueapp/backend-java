package com.example.paceleague.ranking.repository;

public interface RankingProjection {
    Long getMemberSno();
    String getNickname();
    int getTotalScore();
    String getTier();
}

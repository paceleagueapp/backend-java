package com.paceleague.ranking.application.port.out;

public interface RankingProjection {
    Long getMemberSno();
    String getNickname();
    int getTotalScore();
    String getTier();
}

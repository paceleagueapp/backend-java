package com.example.paceleague.rank.application.port.in;

import com.example.paceleague.rank.application.dto.RankMeResponse;

public interface GetMyRankUseCase {
    RankMeResponse getMyRank(Long memberSno, String lang);
}

package com.paceleague.rank.application.port.in;

import com.paceleague.rank.application.dto.RankMeResponse;

public interface GetMyRankUseCase {
    RankMeResponse getMyRank(Long memberSno, String lang);
}

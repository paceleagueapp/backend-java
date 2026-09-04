package com.paceleague.ranking.application.port.in;

import com.paceleague.ranking.application.dto.RankingPageResponse;
import com.paceleague.ranking.application.dto.RankingUserResponse;

import java.util.List;

public interface GetRankingUseCase {
    List<RankingUserResponse> getTop10(String lang);

    RankingPageResponse getRankingPage(Long memberSno, String lang);
}

package com.paceleague.rank.application.port.in;

import com.paceleague.rank.domain.enums.RankTier;

public interface GetMemberTierPort {
    RankTier getTier(Long memberSno);
}

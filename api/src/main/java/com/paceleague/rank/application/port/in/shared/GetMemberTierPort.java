package com.paceleague.rank.application.port.in.shared;

import com.paceleague.rank.domain.enums.RankTier;

public interface GetMemberTierPort {
    RankTier getTier(Long memberSno);
}

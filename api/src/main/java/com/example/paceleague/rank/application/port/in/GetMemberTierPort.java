package com.example.paceleague.rank.application.port.in;

import com.example.paceleague.rank.domain.enums.RankTier;

public interface GetMemberTierPort {
    RankTier getTier(Long memberSno);
}

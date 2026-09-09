package com.paceleague.rank.application.service;

import com.paceleague.rank.application.port.in.shared.PurgeMemberRankPort;
import com.paceleague.rank.application.port.out.MemberScoreRepositoryPort;
import com.paceleague.rank.application.port.out.RankRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankPurgeService implements PurgeMemberRankPort {

    private final RankRepositoryPort rankRepositoryPort;
    private final MemberScoreRepositoryPort memberScoreRepositoryPort;

    @Override
    @Transactional
    public void purge(Long memberSno) {
        rankRepositoryPort.deleteAllByUno(memberSno);
        memberScoreRepositoryPort.deleteAllByMemberSno(memberSno);
    }
}

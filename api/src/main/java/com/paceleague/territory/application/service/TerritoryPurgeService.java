package com.paceleague.territory.application.service;

import com.paceleague.territory.application.port.in.shared.PurgeMemberTerritoryPort;
import com.paceleague.territory.application.port.out.TerritoryHexRepositoryPort;
import com.paceleague.territory.application.port.out.TerritoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TerritoryPurgeService implements PurgeMemberTerritoryPort {

    private final TerritoryHexRepositoryPort territoryHexRepositoryPort;
    private final TerritoryRepositoryPort territoryRepositoryPort;

    @Override
    @Transactional
    public void purge(Long memberSno) {
        territoryHexRepositoryPort.deleteAllByOwnerMemberSno(memberSno); // Territory 삭제 전에
        territoryRepositoryPort.deleteAllByOwnerMemberSno(memberSno);
    }
}

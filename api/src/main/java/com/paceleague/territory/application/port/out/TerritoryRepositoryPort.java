package com.paceleague.territory.application.port.out;

import com.paceleague.territory.application.dto.TerritoryOwnerArea;
import com.paceleague.territory.domain.entity.Territory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TerritoryRepositoryPort {

    Territory save(Territory territory);

    Optional<Territory> findBySno(Long sno);

    // 지도 조회용 — bbox가 요청 영역과 겹치는 ACTIVE 땅, 면적 큰 순, 최대 limit개.
    List<Territory> findActiveIntersectingBbox(BigDecimal minLat, BigDecimal minLng,
                                              BigDecimal maxLat, BigDecimal maxLng, int limit);

    // 땅 판정용 — 이번 러닝의 헥사곤 집합과 겹친 것으로 확인된 territory들을 비관적 락으로 조회(동시 공격 직렬화).
    // sno 오름차순으로 반환되어 여러 러닝이 겹칠 때 락 획득 순서가 일정하다(데드락 방지).
    List<Territory> findAllByIdForUpdate(List<Long> snos);

    // 면적 랭킹용 — 소유자별 총 점령 면적(ACTIVE) 내림차순, 최대 limit명.
    List<TerritoryOwnerArea> findTopOwnersByArea(int limit);
}

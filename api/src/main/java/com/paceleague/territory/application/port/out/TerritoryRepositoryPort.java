package com.paceleague.territory.application.port.out;

import com.paceleague.territory.application.dto.TerritoryCaptureCount;
import com.paceleague.territory.application.dto.TerritoryOwnerArea;
import com.paceleague.territory.domain.entity.Territory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TerritoryRepositoryPort {

    Territory save(Territory territory);

    // 땅의 헥사곤을 전부 뺏겨 하나도 안 남았을 때(부분 점령의 극단값) 이 행 자체를 지운다.
    void delete(Territory territory);

    Optional<Territory> findBySno(Long sno);

    // 지도 조회용 — bbox가 요청 영역과 겹치는 ACTIVE 땅, 면적 큰 순, 최대 limit개.
    List<Territory> findActiveIntersectingBbox(BigDecimal minLat, BigDecimal minLng,
                                              BigDecimal maxLat, BigDecimal maxLng, int limit);

    // 땅 판정용 — 이번 러닝의 헥사곤 집합과 겹친 것으로 확인된 territory들을 비관적 락으로 조회(동시 공격 직렬화).
    // sno 오름차순으로 반환되어 여러 러닝이 겹칠 때 락 획득 순서가 일정하다(데드락 방지).
    List<Territory> findAllByIdForUpdate(List<Long> snos);

    // 면적 랭킹용 — 소유자별 총 점령 면적(ACTIVE) 내림차순, 최대 limit명.
    List<TerritoryOwnerArea> findTopOwnersByArea(int limit);

    // 관리자 랭킹관리(랜드잇랭킹 탭) — 위와 같은 집계를 캡 없이 페이지네이션.
    Page<TerritoryOwnerArea> findOwnersByAreaPaged(Pageable pageable);

    // 관리자 랜드잇데이터관리 화면 — 전체 ACTIVE 땅 목록, 최신순 페이지네이션.
    Page<Territory> findAllActiveForAdmin(Pageable pageable);

    // 일간 요약 푸시용 — create_at 이 [from, to) 인 territory 개수 / 서로 다른 소유자 수.
    TerritoryCaptureCount countCapturesBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive);

    // 헥사곤 백필 대상 — territory_hex 행이 하나도 없는 ACTIVE 땅(H3 도입 전에 생성된 "유령 땅" 후보).
    // 오래된 것부터(생성일 오름차순) 처리해 먼저 생긴 땅이 겹치는 헥사곤을 먼저 차지하게 한다.
    List<Territory> findActiveMissingHex();

    // territory 재생(historical replay, 2026-09-07) 전용 — 기존 territory 전부를 지우고 처음부터
    // 다시 만든다. TerritoryHistoricalReplayService 외에는 호출하지 않는다.
    void deleteAll();

    // 회원 탈퇴 시 그 회원 소유 땅 삭제(헥사곤은 TerritoryHexRepositoryPort 에서 먼저).
    void deleteAllByOwnerMemberSno(Long memberSno);
}

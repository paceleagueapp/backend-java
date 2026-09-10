package com.paceleague.territory.application.port.in.shared;

import com.paceleague.territory.application.dto.AdminTerritoryRankingEntry;
import com.paceleague.territory.application.dto.AdminTerritorySummary;
import org.springframework.data.domain.Page;

// admin 도메인(관리자 웹패널 랭킹관리-랜드잇랭킹 탭 / 랜드잇데이터관리 화면)이 쓰는 크로스 도메인 포트.
public interface AdminTerritoryQueryPort {
    // 소유자별 총 점령 면적 랭킹, 캡 없이 페이지네이션(공개 GetTerritoryRankingUseCase는 top-N 캡 고정이라 별도).
    Page<AdminTerritoryRankingEntry> getRankingPage(int page, int size);

    // 전체 ACTIVE 땅 목록, 최신순.
    Page<AdminTerritorySummary> listTerritories(int page, int size);
}

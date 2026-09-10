package com.paceleague.ranking.application.port.in.shared;

import com.paceleague.ranking.application.dto.AdminRankingEntry;
import org.springframework.data.domain.Page;

// admin 도메인(관리자 웹패널 랭킹관리 - 기록랭킹 탭)이 현재 시즌 전체 랭킹을 페이지네이션 조회하기 위한 포트.
public interface AdminRankingQueryPort {
    Page<AdminRankingEntry> getRankingPage(int page, int size);
}

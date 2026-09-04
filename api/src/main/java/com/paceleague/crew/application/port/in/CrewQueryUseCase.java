package com.paceleague.crew.application.port.in;

import com.paceleague.crew.application.dto.CrewDetailResponse;
import com.paceleague.crew.application.dto.CrewRankingEntryResponse;
import com.paceleague.crew.application.dto.CrewSummaryResponse;

import java.util.List;

public interface CrewQueryUseCase {

    // 크루명 검색(공개). q 가 비면 최근 생성/인원 기준 목록.
    List<CrewSummaryResponse> search(String q, String lang);

    // 크루 상세. viewerMemberSno 가 크루원이면 공지·크루원 목록 포함, 아니면 공개 정보만.
    CrewDetailResponse getDetail(Long crewSno, Long viewerMemberSno, String lang);

    // 내 크루. 없으면 null.
    CrewDetailResponse getMyCrew(Long memberSno, String lang);

    // 크루원 기록저장 랭킹(현재 시즌 점수 기준). 크루원만 조회 가능.
    List<CrewRankingEntryResponse> getRanking(Long viewerMemberSno, Long crewSno, String lang);
}

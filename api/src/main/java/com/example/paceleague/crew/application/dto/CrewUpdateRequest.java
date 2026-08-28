package com.example.paceleague.crew.application.dto;

// PUT /api/crew/{sno} — 크루장이 크루 정보를 통째로 갱신한다(클라이언트가 현재 값을 채워 보냄).
//  - name: 필수(검증)
//  - 아이콘: iconMediaId 가 있으면 그 APPROVED media로 교체 / 없고 iconUrl(현재 아이콘 URL 그대로)이 오면 유지 / 둘 다 없으면 제거
//  - description / notice: null 이면 비움
public record CrewUpdateRequest(
        String name,
        Long iconMediaId,
        String iconUrl,
        String description,
        String notice
) {
}

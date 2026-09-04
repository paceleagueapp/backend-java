package com.paceleague.crew.application.dto;

// iconMediaId: 크루 아이콘. 미리 media 업로드(presign→complete)로 APPROVED 된 media의 sno. 없으면 아이콘 없음.
public record CrewCreateRequest(
        String name,
        Long iconMediaId,
        String description
) {
}

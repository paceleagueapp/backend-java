package com.paceleague.popup.application.dto;

import com.paceleague.popup.domain.entity.Popup;
import com.paceleague.popup.domain.enums.PopupTarget;

import java.time.LocalDateTime;

// 공개 조회(GET /api/popup) 응답 한 건 — 지금 노출해야 하는 팝업만 내려가므로 active 는 빼고,
// 클라이언트가 자기 플랫폼으로 거를 수 있게 target 을 준다. startAt/endAt 은 KST.
public record PopupResponse(
        Long sno,
        String title,
        String content,
        PopupTarget target,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
    public static PopupResponse from(Popup p) {
        return new PopupResponse(p.getSno(), p.getTitle(), p.getContent(), p.getTarget(), p.getStartAt(), p.getEndAt());
    }
}

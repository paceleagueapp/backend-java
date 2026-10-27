package com.paceleague.popup.application.dto;

import com.paceleague.popup.domain.entity.Popup;
import com.paceleague.popup.domain.enums.PopupTarget;

import java.time.LocalDateTime;

// 관리자 팝업관리 목록/상세. visibleNow = 활성 + 지금(KST)이 기간 안 → 실제로 노출 중인지.
public record AdminPopupResponse(
        Long sno,
        String title,
        String content,
        PopupTarget target,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean active,
        boolean visibleNow,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
    public static AdminPopupResponse from(Popup p, LocalDateTime now) {
        return new AdminPopupResponse(p.getSno(), p.getTitle(), p.getContent(), p.getTarget(),
                p.getStartAt(), p.getEndAt(), p.isActive(), p.isVisibleAt(now), p.getCreateAt(), p.getUpdateAt());
    }
}

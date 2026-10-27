package com.paceleague.admin.application.dto;

import com.paceleague.popup.application.dto.PopupCommand;
import com.paceleague.popup.domain.enums.PopupTarget;

import java.time.LocalDateTime;

// 관리자 팝업 등록/수정 요청. startAt/endAt 은 KST("2026-11-01T09:00"), active 생략 시 true.
public record PopupSaveRequest(
        String title,
        String content,
        PopupTarget target,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Boolean active
) {
    public PopupCommand toCommand() {
        return new PopupCommand(title, content, target, startAt, endAt, active);
    }
}

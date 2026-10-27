package com.paceleague.popup.application.dto;

import com.paceleague.popup.domain.enums.PopupTarget;

import java.time.LocalDateTime;

// 관리자 팝업 등록/수정 입력값. startAt/endAt 은 KST.
public record PopupCommand(
        String title,
        String content,
        PopupTarget target,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Boolean active
) {
}

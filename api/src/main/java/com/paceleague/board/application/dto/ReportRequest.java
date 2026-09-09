package com.paceleague.board.application.dto;

// reason: SPAM / ABUSE / SEXUAL / ETC (ReportReason enum). detail 선택(최대 500자).
public record ReportRequest(String reason, String detail) {
}

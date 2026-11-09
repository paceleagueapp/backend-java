package com.paceleague.notification.application.dto;

import java.time.LocalDateTime;
import java.util.List;

// 관리자 알림관리 목록 1행. contentPreview = 본문 평문 요약(푸시 본문과 같은 값),
// targetNicknames = 받는 회원 앞쪽 몇 명(나머지는 targetCount 로 "외 N명" 표시).
public record AdminNotificationSummary(
        Long sno,
        String title,
        String contentPreview,
        int targetCount,
        int sentCount,
        List<String> targetNicknames,
        LocalDateTime createdAt
) {}

package com.paceleague.admin.application.dto;

import java.util.List;

// content = 게시글 에디터 HTML(서버에서 정제), memberSnos = 받는 회원(최대 1000명).
public record AdminNotificationSendRequest(
        String title,
        String content,
        List<Long> memberSnos
) {}

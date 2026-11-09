package com.paceleague.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 관리자 알림관리에서 회원을 골라 보낸 알림 1건. 받는 회원은 admin_notification_target.
// content 는 게시글 에디터 HTML(PostContentSanitizer 로 정제된 값) — 푸시 본문은 여기서 뽑은 평문 요약이고,
// 앱은 푸시 딥링크로 GET /api/notification/notice/{sno} 를 불러 이미지까지 보여준다.
@Entity
@Table(name = "admin_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "target_count", nullable = false)
    private int targetCount;

    // 1대 이상 실제로 발송된 회원 수(알림 거부/탈퇴/토큰 없음은 제외).
    @Column(name = "sent_count", nullable = false)
    private int sentCount;

    @Column(name = "created_by_admin_sno", nullable = false)
    private Long createdByAdminSno;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private AdminNotification(String title, String content, int targetCount, Long createdByAdminSno) {
        this.title = title;
        this.content = content;
        this.targetCount = targetCount;
        this.sentCount = 0;
        this.createdByAdminSno = createdByAdminSno;
        this.createdAt = LocalDateTime.now();
    }

    public static AdminNotification create(String title, String content, int targetCount, Long createdByAdminSno) {
        return new AdminNotification(title, content, targetCount, createdByAdminSno);
    }

    public void recordSent(int sentCount) {
        this.sentCount = sentCount;
    }
}

package com.paceleague.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 알림내역 1건 = 실제로 발송된 푸시 1건. 웹/앱의 알림내역 리스트가 이 테이블을 읽는다.
// memberSno == null 이면 전체 발송(토픽 브로드캐스트) — 모든 회원에게 보이되 가입 이후 발송분만 노출.
@Entity
@Table(name = "notification_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "member_sno")
    private Long memberSno;

    @Column(name = "kind", nullable = false, length = 40)
    private String kind;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Column(name = "deeplink", length = 300)
    private String deeplink;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private NotificationHistory(Long memberSno, String kind, String title, String body, String deeplink) {
        this.memberSno = memberSno;
        this.kind = kind;
        this.title = title;
        this.body = body;
        this.deeplink = deeplink;
        this.createdAt = LocalDateTime.now();
    }

    public static NotificationHistory broadcast(String kind, String title, String body, String deeplink) {
        return new NotificationHistory(null, kind, title, body, deeplink);
    }
}

package com.paceleague.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 관리자 알림의 받는 회원 1명. sent = 실제로 1대 이상 발송됐는지.
// 앱 상세 조회(GET /api/notification/notice/{sno})도 이 행이 있는 회원에게만 허용한다.
@Entity
@Table(name = "admin_notification_target")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminNotificationTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "notification_sno", nullable = false)
    private Long notificationSno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(nullable = false)
    private boolean sent;

    private AdminNotificationTarget(Long notificationSno, Long memberSno, boolean sent) {
        this.notificationSno = notificationSno;
        this.memberSno = memberSno;
        this.sent = sent;
    }

    public static AdminNotificationTarget of(Long notificationSno, Long memberSno, boolean sent) {
        return new AdminNotificationTarget(notificationSno, memberSno, sent);
    }
}

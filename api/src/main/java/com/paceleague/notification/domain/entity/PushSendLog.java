package com.paceleague.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 전체 발송 푸시 1건의 이력. (kind, target_date) 유니크가 "하루 1회" 를 강제해 재시작/다중 인스턴스에서
// 중복 발송을 막는 가드이자 감사 로그.
//  - RESERVED: 발송 시도를 선점함(집계·발송 전)
//  - SENT: FCM 발송 성공 (fcmMessageId 채워짐)
//  - SKIPPED: 발송 안 함 (0건인 날 / FCM 미설정 등, detail 에 사유)
//  - FAILED: 발송 시도했으나 실패 (detail 에 사유). 같은 target_date 재시도 시 다시 RESERVED 로 되돌려 준다.
@Entity
@Table(name = "push_send_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushSendLog {

    public static final String STATUS_RESERVED = "RESERVED";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_SKIPPED = "SKIPPED";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "kind", nullable = false, length = 40)
    private String kind;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "body", length = 500)
    private String body;

    @Column(name = "payload_json", length = 1000)
    private String payloadJson;

    @Column(name = "fcm_message_id", length = 200)
    private String fcmMessageId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private PushSendLog(String kind, LocalDate targetDate) {
        this.kind = kind;
        this.targetDate = targetDate;
        this.status = STATUS_RESERVED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static PushSendLog reserve(String kind, LocalDate targetDate) {
        return new PushSendLog(kind, targetDate);
    }

    // FAILED 이력을 재시도용으로 되돌린다.
    public void reReserve() {
        this.status = STATUS_RESERVED;
        this.detail = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markSent(String title, String body, String payloadJson, String fcmMessageId) {
        this.status = STATUS_SENT;
        this.title = title;
        this.body = body;
        this.payloadJson = payloadJson;
        this.fcmMessageId = fcmMessageId;
        this.detail = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markSkipped(String title, String body, String detail) {
        this.status = STATUS_SKIPPED;
        this.title = title;
        this.body = body;
        this.detail = detail;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String title, String body, String detail) {
        this.status = STATUS_FAILED;
        this.title = title;
        this.body = body;
        this.detail = detail == null ? null : detail.substring(0, Math.min(detail.length(), 500));
        this.updatedAt = LocalDateTime.now();
    }
}

package com.paceleague.notification.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 회원 기기 1대의 FCM 토큰. "전체 회원" 푸시는 이 토큰들로 보낸다(토픽 대신 — 비회원 설치자 제외).
// fcmId 가 유니크라 같은 기기에서 다른 계정으로 로그인하면 행을 새로 만들지 않고 회원만 바꾼다.
@Entity
@Table(name = "member_device_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "fcm_id", nullable = false, unique = true, length = 255)
    private String fcmId;

    // ANDROID / IOS
    @Column(name = "device", nullable = false, length = 10)
    private String device;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private DeviceToken(Long memberSno, String fcmId, String device) {
        this.memberSno = memberSno;
        this.fcmId = fcmId;
        this.device = device;
        this.createdAt = LocalDateTime.now();
        this.lastSeenAt = this.createdAt;
    }

    public static DeviceToken create(Long memberSno, String fcmId, String device) {
        return new DeviceToken(memberSno, fcmId, device);
    }

    // 재등록 — 다른 계정으로 로그인했으면 회원이 바뀌고, 같은 계정이면 last_seen_at 만 갱신된다.
    public void touch(Long memberSno, String device) {
        this.memberSno = memberSno;
        this.device = device;
        this.lastSeenAt = LocalDateTime.now();
    }
}

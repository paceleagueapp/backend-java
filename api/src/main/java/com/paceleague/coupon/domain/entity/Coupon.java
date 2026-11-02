package com.paceleague.coupon.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 관리자가 회원 1명에게 지급한 기프티콘 쿠폰. 이미지는 S3 비공개 경로(coupon/)에 두고,
// 조회할 때마다 짧은 presigned GET URL 을 만들어 내려준다(바코드가 곧 돈이라 공개 URL 을 쓰지 않음).
// 사용 여부는 회원이 직접 "사용 완료"로 표시한다 — 기프티콘은 서버에서 실제 사용을 검증할 수 없다.
@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    public static final String STATUS_ISSUED = "ISSUED";
    public static final String STATUS_USED = "USED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "image_key", nullable = false, length = 200, unique = true)
    private String imageKey;

    // 이 날짜(KST)까지 사용 가능.
    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "granted_by_admin_sno", nullable = false)
    private Long grantedByAdminSno;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Coupon(Long memberSno, String name, String imageKey, LocalDate expiresAt, Long grantedByAdminSno) {
        this.memberSno = memberSno;
        this.name = name;
        this.imageKey = imageKey;
        this.expiresAt = expiresAt;
        this.grantedByAdminSno = grantedByAdminSno;
        this.status = STATUS_ISSUED;
        this.createdAt = LocalDateTime.now();
    }

    public static Coupon grant(Long memberSno, String name, String imageKey, LocalDate expiresAt, Long grantedByAdminSno) {
        return new Coupon(memberSno, name, imageKey, expiresAt, grantedByAdminSno);
    }

    public boolean isUsed() {
        return STATUS_USED.equals(status);
    }

    public boolean isExpired(LocalDate today) {
        return today.isAfter(expiresAt);
    }

    public void markUsed(LocalDate today) {
        if (isUsed()) {
            throw new IllegalArgumentException("Coupon has already been used.");
        }
        if (isExpired(today)) {
            throw new IllegalArgumentException("Coupon has expired.");
        }
        this.status = STATUS_USED;
        this.usedAt = LocalDateTime.now();
    }
}

package com.paceleague.coupon.application.dto;

import com.paceleague.coupon.domain.entity.Coupon;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 회원 쿠폰함 1건. imageUrl 은 짧게(10분) 유효한 presigned URL — 앱은 저장하지 말고 열 때마다 목록을 다시 받는다.
public record CouponResponse(
        Long sno,
        String name,
        String imageUrl,
        LocalDate expiresAt,   // 이 날짜(KST)까지 사용 가능
        String status,         // ISSUED / USED
        boolean expired,
        LocalDateTime usedAt,
        LocalDateTime createdAt
) {
    public static CouponResponse of(Coupon c, String imageUrl, LocalDate today) {
        return new CouponResponse(c.getSno(), c.getName(), imageUrl, c.getExpiresAt(), c.getStatus(),
                c.isExpired(today), c.getUsedAt(), c.getCreatedAt());
    }
}

package com.paceleague.coupon.application.dto;

// 관리자 쿠폰 이미지 업로드 준비 — 브라우저가 uploadUrl 로 PUT 한 뒤 imageKey 를 지급 요청에 싣는다.
public record CouponImageUploadResponse(
        String uploadUrl,
        String imageKey,
        long expiresInSeconds
) {}

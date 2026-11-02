package com.paceleague.coupon.application.port.in.shared;

import com.paceleague.coupon.application.dto.CouponImageUploadResponse;
import com.paceleague.coupon.application.dto.CouponResponse;

import java.time.LocalDate;
import java.util.List;

// admin 도메인(회원 상세 화면의 쿠폰 지급)이 쓰는 크로스 도메인 포트.
public interface AdminCouponUseCase {

    CouponImageUploadResponse requestImageUpload(String mimeType, Long fileSizeBytes);

    CouponResponse grant(Long adminSno, Long memberSno, String name, LocalDate expiresAt, String imageKey);

    List<CouponResponse> listByMember(Long memberSno);

    void delete(Long couponSno);
}

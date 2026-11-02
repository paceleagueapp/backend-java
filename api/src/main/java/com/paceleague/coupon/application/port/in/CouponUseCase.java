package com.paceleague.coupon.application.port.in;

import com.paceleague.coupon.application.dto.CouponResponse;

import java.util.List;

// 회원 쿠폰함(앱).
public interface CouponUseCase {

    List<CouponResponse> listMine(Long memberSno);

    CouponResponse markUsed(Long memberSno, Long couponSno);
}
